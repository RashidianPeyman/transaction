package ir.navaco.cart.transaction.serviceImpl;


import ir.navaco.cart.transaction.CartTransaction;
import ir.navaco.cart.transaction.service.Business;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class BusinessImpl implements Business {
    private final CartService cartService;
    private final TransactionService transactionService;

    @Transactional
    public void performRollback(String rrn) {
        CartTransaction transaction = transactionService.rollbackTransaction(rrn);
        cartService.depositCart(transaction.getCartId(), transaction.getRealUsed(), transaction.getCreditUsed());
    }

    @Transactional
    public void performDeposit(Long cartId, BigDecimal amount) {
        cartService.depositCart(cartId, amount, BigDecimal.ZERO);
        transactionService.
    }
}
