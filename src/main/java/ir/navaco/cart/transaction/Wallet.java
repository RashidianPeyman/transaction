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

@Entity
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Wallet {
    @Id
    private Long id;

    @Column(precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "blocked_balance", precision = 19, scale = 4)
    private BigDecimal blockedBalance;

    private String status; // ACTIVE, INACTIVE

    // Getters and Setters
}