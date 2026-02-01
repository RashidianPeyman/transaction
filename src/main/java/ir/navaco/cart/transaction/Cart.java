package ir.navaco.cart.transaction;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Entity
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@DynamicUpdate
public class Cart {
    @Id
    private Long id;

    @Column(name = "real_balance", precision = 19, scale = 4)
    private BigDecimal realBalance;

    @Column(name = "credit_balance", precision = 19, scale = 4)
    private BigDecimal creditBalance;

    @Column(name = "credit_blocked", precision = 19, scale = 4)
    private BigDecimal creditBlocked;

    @Column(name = "real_blocked", precision = 19, scale = 4)
    private BigDecimal realBlocked;


    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "blocked_balance", precision = 19, scale = 4)
    private BigDecimal blockedBalance;

    private String status; // ACTIVE, INACTIVE

    // Getters and Setters
}