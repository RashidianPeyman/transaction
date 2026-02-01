package ir.navaco.cart.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class CreditRealUsed {
    private final BigDecimal creditUsed;
    private final BigDecimal realUsed;

    @Override
    public String toString() {
        return "CreditRealUsed{" +
                "creditUsed=" + creditUsed +
                ", realUsed=" + realUsed +
                '}';
    }
}
