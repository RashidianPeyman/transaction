package ir.navaco.cart.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartTransaction {
    @Id
    private String id; // UUID داخلی ما

    private Long cartId;

    private Long accountNumber;

    private BigDecimal amount;

    private String status; // PENDING, SUCCESS, FAILED


    private Long stan;

    private String rrn;
    private String authCode;
    private String mcc;
    private String currency;
    private Long transactionType; // این میگه مثلا اگه deposit بوده چی بوده Refund reverse rollbakc
    private Long majorType; // این فقط میگه debit بوده یا deposit
    private String terminalId;
    private String merchantId;

    private BigDecimal creditUsed;
    private BigDecimal realUsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
}
