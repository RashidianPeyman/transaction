package ir.navaco.cart.transaction.serviceImpl;

import ir.navaco.cart.transaction.*;
import ir.navaco.cart.transaction.queries.CartSql;
import ir.navaco.cart.transaction.repositories.CartRepositoryJpa;
import ir.navaco.spring.starter.common.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final JdbcTemplate jdbcTemplate;
    private final CartRepository cartRepository;
    private final CartRepositoryJpa cartRepositoryJpa;


    // تعریف RowMapper به صورت دستی برای بالاترین پرفورمنس
    private final RowMapper<Cart> CartRowMapper = (rs, rowNum) -> {
        Cart cart = new Cart();
        cart.setId(rs.getLong("id"));
        cart.setCreditBalance(rs.getBigDecimal("credit_balance"));
        cart.setRealBalance(rs.getBigDecimal("real_balance"));
        cart.setCreditBlocked(rs.getBigDecimal("credit_blocked"));
        cart.setRealBlocked(rs.getBigDecimal("real_blocked"));

        // برای استفاده در حالت تکی
        cart.setBalance(rs.getBigDecimal("balance"));
        cart.setBlockedBalance(rs.getBigDecimal("blocked_balance"));

        cart.setStatus(rs.getString("status"));
        return cart;
    };

    @Transactional
    public PreAuthorizeResponse processCallback(ProcessRequest processRequest) {
        //  تسهیم تراکنش
        cartChecking(processRequest);

        // ۲. ثبت تراکنش در وضعیت PENDING
        Long stan = Long.valueOf(Utils.generateStan());
        String authCode = Utils.generateAuthCodeAlphaNumeric();
        String id = UUID.randomUUID().toString();
        String insertTxSql = """
                INSERT INTO transactions (id, account_number, amount,stan,rrn,auth_code,mcc,currency,transaction_type, terminal_id, merchant_id, status, created_at) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(insertTxSql, id, processRequest.cartId(), processRequest.amount(), stan, processRequest.rrn(), authCode,
                processRequest.mcc(), processRequest.currency(), processRequest.transactionType(), processRequest.terminalId(), processRequest.merchantId());

        confirm(1l, id);

        PreAuthorizeResponse preAuthorizeResponse = new PreAuthorizeResponse(stan, authCode, processRequest.amount(),
                processRequest.currency(), "014", processRequest.rrn(), false, Instant.now());


        return preAuthorizeResponse;

    }

    private void confirm(Long txId, String id) {
        // کسر نهایی از مبلغ بلوکه شده
        String updateCartSql = """
                UPDATE Cart w 
                SET w.blocked_balance = w.blocked_balance - (SELECT t.amount FROM transactions t WHERE t.id = ?)
                WHERE w.id = (SELECT t.account_number FROM transactions t WHERE t.id = ?)
                """;
        int status = jdbcTemplate.update(updateCartSql, id, id);

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

    private int cartChecking(ProcessRequest processRequest) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("amount", processRequest.amount())
                .addValue("id", 1);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        int affectedRows = namedParameterJdbcTemplate.update(CartSql.preAuthorize_SQL, params);

        if (affectedRows == 0) {
            throw new RuntimeException("تراکنش مجاز نیست: موجودی کافی نیست یا کارت غیرفعال است.");
        }
        return affectedRows;

    }

    @Transactional
    public void processTransaction(ProcessRequest request) {
        long cartId = request.cartId();
        BigDecimal amount = request.amount();

        // --------------------------
        // 1. محاسبه موجودی credit و real قبل از update
        // --------------------------
        CreditRealUsed balances = cartRepository.getCreditRealBalances(cartId);

        if (balances.getCreditUsed().add(balances.getRealUsed()).compareTo(amount) < 0) {
            throw new RuntimeException("gg------------------");
        }

        // محاسبه میزان debit از credit و real
        BigDecimal creditUsed = balances.getCreditUsed().min(amount);
        BigDecimal realUsed = amount.subtract(creditUsed);

        // --------------------------
        // 2. Update کارت (debit اتمیک)
        // --------------------------
        boolean success = cartRepository.debitCart(cartId, amount);
        if (!success) {
            throw new RuntimeException("dd------------------");
        }

        String authCode = Utils.generateAuthCodeAlphaNumeric();
        Long stan = Long.valueOf(Utils.generateStan());
        // --------------------------
        // 3. Insert تراکنش
        // --------------------------
        CartTransaction tx = new CartTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setCartId(cartId);
        tx.setAmount(amount);
        tx.setStan(stan);
        tx.setRrn(request.rrn());
        tx.setAuthCode(authCode);
        tx.setMcc(request.mcc());
        tx.setCurrency(request.currency());
        tx.setTransactionType(request.transactionType());
        tx.setTerminalId(request.terminalId());
        tx.setMerchantId(request.merchantId());
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(LocalDateTime.now());
        tx.setCreditUsed(creditUsed);
        tx.setRealUsed(realUsed);

        cartRepository.insertTransaction(tx);
    }


    @Transactional
    public Cart depositCart(Long cartId, BigDecimal realAmount, BigDecimal creditAmount) {
        Cart cart = cartRepositoryJpa.findById(cartId).orElseThrow(() -> {
            return new RuntimeException(" cart not found");
        });
        cart.setRealBalance(cart.getRealBalance().add(realAmount));
        cart.setCreditBalance(cart.getCreditBalance().add(creditAmount));
        return cartRepositoryJpa.save(cart);
    }

    @Transactional
    public Cart debitCart(Long cartId, BigDecimal realAmount, BigDecimal creditAmount) {
        // 1. پیدا کردن کارت با PESSIMISTIC WRITE (lock ردیف)
        Cart cart = cartRepositoryJpa.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 2. بررسی موجودی کافی قبل از کم کردن
        if (cart.getRealBalance().compareTo(realAmount) < 0) {
            throw new RuntimeException("Insufficient real balance");
        }

        if (cart.getCreditBalance().compareTo(creditAmount) < 0) {
            throw new RuntimeException("Insufficient credit balance");
        }

        // 3. کم کردن موجودی
        cart.setRealBalance(cart.getRealBalance().subtract(realAmount));
        cart.setCreditBalance(cart.getCreditBalance().subtract(creditAmount));

        // 4. ذخیره کارت (اختیاری اگر persistence context فعال است)
        return cartRepositoryJpa.save(cart);
    }


}

