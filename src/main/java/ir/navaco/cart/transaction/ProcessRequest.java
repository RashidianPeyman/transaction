package ir.navaco.cart.transaction;


import java.io.Serializable;
import java.math.BigDecimal;

public record ProcessRequest(String rrn, Long cartId, String mcc,
                             BigDecimal amount, String currency, Long transactionType, String terminalId,
                             String merchantId) implements Serializable {
}
