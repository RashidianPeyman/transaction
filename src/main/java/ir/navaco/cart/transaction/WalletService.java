package ir.navaco.cart.transaction;

import ir.navaco.spring.starter.common.Utils;
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
    private final RowMapper<Wallet> walletRowMapper = (rs, rowNum) -> {
        Wallet wallet = new Wallet();
        wallet.setId(rs.getLong("id"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setBlockedBalance(rs.getBigDecimal("blocked_balance"));
        wallet.setStatus(rs.getString("status"));
        return wallet;
    };

    /**
     * مرحله اول: استعلام و رزرو موجودی
     */
    @Transactional
    public PreAuthorizeResponse preAuthorize(Long walletId, BigDecimal amount) {

        String updateWalletSql = """
                UPDATE wallets 
                SET balance = balance - ?, 
                    blocked_balance = blocked_balance + ? 
                WHERE id = ? AND status = 'ACTIVE' AND balance >= ?
                """;

        int affectedRows = jdbcTemplate.update(updateWalletSql, amount, amount, walletId, amount);
        //TODO اگه گفتن میتونم همینجا شماره پیگیری هم بفرستم چک کن
        if (affectedRows == 0) {
            throw new RuntimeException("تراکنش مجاز نیست: موجودی کافی نیست یا کارت غیرفعال است.");
        }

        // ۲. ثبت تراکنش در وضعیت PENDING
        String internalTxId = UUID.randomUUID().toString();
        Long stan = Long.valueOf(Utils.generateStan());
        String authCode = Utils.generateAuthCodeAlphaNumeric();
        String insertTxSql = """
                INSERT INTO transactions (id, wallet_id, amount, status, created_at) 
                VALUES (?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(insertTxSql, internalTxId, walletId, amount);

        PreAuthorizeResponse preAuthorizeResponse = new PreAuthorizeResponse(stan, authCode, amount, "IRR", "014", false, Instant.now());

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
            String updateWalletSql = """
                    UPDATE wallets w 
                    SET w.blocked_balance = w.blocked_balance - (SELECT t.amount FROM transactions t WHERE t.id = ?)
                    WHERE w.id = (SELECT t.wallet_id FROM transactions t WHERE t.id = ?)
                    """;
            int status = jdbcTemplate.update(updateWalletSql, txId, txId);
        } else {
            //TODO چک کن بره تو Settlement یا همینجا مبلغ رو آزاد کنه
        }
    }

    private void rollback(String txId, String pspRef) {
        String updateTxSql = "UPDATE transactions SET status = 'FAILED', psp_reference_id = ? WHERE id = ? AND status = 'PENDING'";
        int updated = jdbcTemplate.update(updateTxSql, pspRef, txId);

        if (updated > 0) {
            // بازگرداندن پول از بلوکه به موجودی اصلی
            String updateWalletSql = """
                    UPDATE wallets w 
                    SET w.balance = w.balance + (SELECT t.amount FROM transactions t WHERE t.id = ?),
                        w.blocked_balance = w.blocked_balance - (SELECT t.amount FROM transactions t WHERE t.id = ?)
                    WHERE w.id = (SELECT t.wallet_id FROM transactions t WHERE t.id = ?)
                    """;
            int status = jdbcTemplate.update(updateWalletSql, txId, txId, txId);
        }
    }

    public Wallet getWalletDetails(Long walletId) {
        String sql = "SELECT id, balance, blocked_balance, status FROM wallets WHERE id = ?";

        try {
            // اینجا دقیقا از RowMapper دستی استفاده می‌کنیم برای بالاترین سرعت
            return jdbcTemplate.queryForObject(sql, walletRowMapper, walletId);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("کیف پول یافت نشد");
        }
    }

    @Transactional
    public WalletResponse handleTransaction(Long walletId, BigDecimal amount) {
        // ۱. عملیات سنگین و حساس بدون مپر (فقط کوئری اتمیک)
        String txId = preAuthorize(walletId, amount);

        // ۲. حالا برای نمایش به کاربر، دیتا را مپ می‌کنیم و می‌خوانیم
        Wallet updatedWallet = getWalletDetails(walletId);

        return WalletResponse.success(txId, updatedWallet);
    }
}

/*
// هنگام ایجاد کیف پول جدید
String sql = "INSERT INTO wallets (id, balance, blocked_balance, status) VALUES (wallet_seq.NEXTVAL, ?, ?, ?)";
jdbcTemplate.update(sql, BigDecimal.ZERO, BigDecimal.ZERO, "ACTIVE");*/
