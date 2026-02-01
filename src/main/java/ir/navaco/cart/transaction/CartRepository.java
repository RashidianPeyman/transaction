package ir.navaco.cart.transaction;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@AllArgsConstructor
public class CartRepository {
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * مرحله اتمیک: موجودی چک و کم میشه
     * return true if success, false if insufficient funds
     */
    public boolean debitCart(long cartId, BigDecimal amount) {
        String sql = """
            UPDATE Cart
            SET
                credit_balance = credit_balance - 
                    CASE WHEN credit_balance >= :amount THEN :amount ELSE credit_balance END,
                real_balance = real_balance -
                    CASE WHEN credit_balance >= :amount THEN 0 ELSE :amount - credit_balance END
            WHERE id = :cartId
              AND status = 'ACTIVE'
              AND (credit_balance + real_balance) >= :amount
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartId)
                .addValue("amount", amount);

        int updatedRows = jdbc.update(sql, params);
        return updatedRows > 0;
    }

    /**
     * درج تراکنش با مقدار استفاده شده از credit و real
     * برای portability: credit_used و real_used محاسبه میشن در جاوا
     */
    public void insertTransaction(CartTransaction tx) {
        String sql = """
            INSERT INTO Transactions
            (id, cart_id, amount, stan, rrn, auth_code, mcc, currency, transaction_type,
             terminal_id, merchant_id, status, created_at, credit_used, real_used)
            VALUES
            (:id, :cartId, :amount, :stan, :rrn, :authCode, :mcc, :currency, :transactionType,
             :terminalId, :merchantId, :status, :createdAt, :creditUsed, :realUsed)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", tx.getId())
                .addValue("cartId", tx.getCartId())
                .addValue("amount", tx.getAmount())
                .addValue("stan", tx.getStan())
                .addValue("rrn", tx.getRrn())
                .addValue("authCode", tx.getAuthCode())
                .addValue("mcc", tx.getMcc())
                .addValue("currency", tx.getCurrency())
                .addValue("transactionType", tx.getTransactionType())
                .addValue("terminalId", tx.getTerminalId())
                .addValue("merchantId", tx.getMerchantId())
                .addValue("status", tx.getStatus())
                .addValue("createdAt", tx.getCreatedAt())
                .addValue("creditUsed", tx.getCreditUsed())
                .addValue("realUsed", tx.getRealUsed());

        jdbc.update(sql, params);
    }

    /**
     * گرفتن مقادیر بلاک نشده فعلی برای محاسبه credit_used و real_used
     */
    public CreditRealUsed getCreditRealBalances(long cartId) {
        String sql = "SELECT credit_balance, real_balance FROM Cart WHERE id = :cartId";

        return jdbc.queryForObject(sql, new MapSqlParameterSource("cartId", cartId), (rs, rowNum) -> {
            BigDecimal creditBalance = rs.getBigDecimal("credit_balance");
            BigDecimal realBalance = rs.getBigDecimal("real_balance");
            return new CreditRealUsed(creditBalance, realBalance);
        });
    }



}

