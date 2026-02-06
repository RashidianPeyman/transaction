package ir.navaco.cart.transaction;


import ir.navaco.spring.starter.common.GeneralUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final JdbcTemplate jdbcTemplate;

    // تعریف RowMapper به صورت دستی برای بالاترین پرفورمنس
    private final RowMapper<Cart> CartRowMapper = (rs, rowNum) -> {
        Cart cart = new Cart();
        cart.setId(rs.getLong("id"));
        cart.setBalance(rs.getBigDecimal("balance"));
        cart.setBlockedBalance(rs.getBigDecimal("blocked_balance"));
        cart.setStatus(rs.getString("status"));
        return cart;
    };

    /**
     * مرحله اول: استعلام و رزرو موجودی
     */
    @Transactional
    public PreAuthorizeResponse preAuthorize(Long CartId, BigDecimal amount) {

        String updateCartSql = """
                UPDATE Cart 
                SET balance = balance - ?, 
                    blocked_balance = blocked_balance + ? 
                WHERE id = ? AND status = 'ACTIVE' AND balance >= ?
                """;

        int affectedRows = jdbcTemplate.update(updateCartSql, amount, amount, CartId, amount);
        //TODO اگه گفتن میتونم همینجا شماره پیگیری هم بفرستم چک کن
        if (affectedRows == 0) {
            throw new RuntimeException("تراکنش مجاز نیست: موجودی کافی نیست یا کارت غیرفعال است.");
        }

        // ۲. ثبت تراکنش در وضعیت PENDING
        String internalTxId = UUID.randomUUID().toString();
        Long stan = Long.valueOf(GeneralUtils.generateStan());
        String authCode = GeneralUtils.generateAuthCodeAlphaNumeric();
        String insertTxSql = """
                INSERT INTO transactions (id, Cart_id, amount, status, created_at) 
                VALUES (?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(insertTxSql, internalTxId, CartId, amount);

        PreAuthorizeResponse preAuthorizeResponse = new PreAuthorizeResponse(stan, authCode, amount, "IRR", "014","RRN" ,false, Instant.now());

        return preAuthorizeResponse;
    }

    /**
     * مرحله دوم: تایید نهایی (نتیجه PSP) با رعایت Idempotency
     */
    @Transactional
    public void processCallback(String internalTxId, String pspRef, boolean success) {
        if (success) {
            confirm(internalTxId, pspRef);
        } else {
            rollback(internalTxId, pspRef);
        }
    }

    private void confirm(String txId, String pspRef) {
        // تغییر وضعیت تراکنش فقط اگر هنوز PENDING باشد (Idempotency)
        String updateTxSql = "UPDATE transactions SET status = 'SUCCESS', psp_reference_id = ? WHERE id = ? AND status = 'PENDING'";
        int updated = jdbcTemplate.update(updateTxSql, pspRef, txId);

        if (updated > 0) {
            // کسر نهایی از مبلغ بلوکه شده
            String updateCartSql = """
                    UPDATE Cart w 
                    SET w.blocked_balance = w.blocked_balance - (SELECT t.amount FROM transactions t WHERE t.id = ?)
                    WHERE w.id = (SELECT t.Cart_id FROM transactions t WHERE t.id = ?)
                    """;
            int status = jdbcTemplate.update(updateCartSql, txId, txId);
        } else {
            //TODO چک کن بره تو Settlement یا همینجا مبلغ رو آزاد کنه
        }
    }

    private void rollback(String txId, String pspRef) {
        String updateTxSql = "UPDATE transactions SET status = 'FAILED', psp_reference_id = ? WHERE id = ? AND status = 'PENDING'";
        int updated = jdbcTemplate.update(updateTxSql, pspRef, txId);

        if (updated > 0) {
            // بازگرداندن پول از بلوکه به موجودی اصلی
            String updateCartSql = """
                    UPDATE Cart w 
                    SET w.balance = w.balance + (SELECT t.amount FROM transactions t WHERE t.id = ?),
                        w.blocked_balance = w.blocked_balance - (SELECT t.amount FROM transactions t WHERE t.id = ?)
                    WHERE w.id = (SELECT t.Cart_id FROM transactions t WHERE t.id = ?)
                    """;
            int status = jdbcTemplate.update(updateCartSql, txId, txId, txId);
        }
    }

    public Cart getCartDetails(Long CartId) {
        String sql = "SELECT id, balance, blocked_balance, status FROM Cart WHERE id = ?";

        try {
            // اینجا دقیقا از RowMapper دستی استفاده می‌کنیم برای بالاترین سرعت
            return jdbcTemplate.queryForObject(sql, CartRowMapper, CartId);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("کیف پول یافت نشد");
        }
    }

    @Transactional
    public CartResponse handleTransaction(Long CartId, BigDecimal amount) {
      /*  // ۱. عملیات سنگین و حساس بدون مپر (فقط کوئری اتمیک)
        String txId = preAuthorize(CartId, amount);

        // ۲. حالا برای نمایش به کاربر، دیتا را مپ می‌کنیم و می‌خوانیم
        Cart updatedCart = getCartDetails(CartId);

        return CartResponse.success(txId, updatedCart);*/

        return null;
    }
}

/*
// هنگام ایجاد کیف پول جدید
String sql = "INSERT INTO Carts (id, balance, blocked_balance, status) VALUES (Cart_seq.NEXTVAL, ?, ?, ?)";
jdbcTemplate.update(sql, BigDecimal.ZERO, BigDecimal.ZERO, "ACTIVE");*/
