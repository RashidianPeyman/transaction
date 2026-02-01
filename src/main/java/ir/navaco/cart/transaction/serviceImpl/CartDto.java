package ir.navaco.cart.transaction.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartDto {
private Long cartId;
private BigDecimal creditBalance;
private BigDecimal realBalance;
}
