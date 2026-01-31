package ir.navaco.cart.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class WalletTransaction {
    @Id
    private String id; // UUID داخلی ما

    @Column(name = "wallet_id")
    private Long walletId;

    private BigDecimal amount;
    private String status; // PENDING, SUCCESS, FAILED

    private Long stan;

    @Column(name = "psp_reference_id")
    private String rrn;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
}
