package ir.navaco.cart.transaction.queries;

public final class CartSql {
    public static final String preAuthorize_SQL = """
            UPDATE Cart
            SET
                credit_balance = credit_balance -
                    CASE
                        WHEN credit_balance >= :amount THEN :amount
                        ELSE credit_balance
                    END,
            
                credit_blocked = credit_blocked +
                    CASE
                        WHEN credit_balance >= :amount THEN :amount
                        ELSE credit_balance
                    END,
            
                real_balance = real_balance -
                    CASE
                        WHEN credit_balance >= :amount THEN 0
                        ELSE :amount - credit_balance
                    END,
            
                real_blocked = real_blocked +
                    CASE
                        WHEN credit_balance >= :amount THEN 0
                        ELSE :amount - credit_balance
                    END
            
            WHERE id = :id
              AND status = 'ACTIVE'
              AND (credit_balance + real_balance) >= :amount
            """;


}
