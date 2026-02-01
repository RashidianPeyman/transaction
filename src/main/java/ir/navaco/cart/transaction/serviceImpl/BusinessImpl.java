package ir.navaco.cart.transaction.serviceImpl;


import ir.navaco.cart.transaction.Cart;
import ir.navaco.cart.transaction.CartTransaction;
import ir.navaco.cart.transaction.ProcessRequest;
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
    public void performDeposit(ProcessRequest request) {
        cartService.depositCart(request.cartId(), request.amount(), BigDecimal.ZERO);
        transactionService.performTransaction(request, request.amount(), 0l, 0l, request.amount(), BigDecimal.ZERO);
    }

    @Transactional
    public void performDebit(ProcessRequest request) {
        cartService.debitCart(request.cartId(), request.amount(), BigDecimal.ZERO);
        transactionService.performTransaction(request, request.amount(), 0l, 0l, request.amount(),BigDecimal.ZERO);
    }


}
