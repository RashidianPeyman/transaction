package ir.navaco.cart.transaction.serviceImpl;

import ir.navaco.cart.transaction.CartTransaction;
import ir.navaco.cart.transaction.ProcessRequest;
import ir.navaco.cart.transaction.repositories.TransactionRepository;
import ir.navaco.spring.starter.common.GeneralUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public CartTransaction rollbackTransaction(String rrn) {
        CartTransaction transaction = transactionRepository.findByRrn(rrn).orElseThrow(() -> {
            return new RuntimeException("Transaction not found");
        });

        //5 rollback
        //0 deposit
        CartTransaction newCartTransaction = updateTransaction(transaction, 5l, 0l);
        return transactionRepository.save(newCartTransaction);
    }

    @Transactional
    public CartTransaction performTransaction(ProcessRequest processRequest, BigDecimal amount, Long transactionType, Long majorType,BigDecimal realBalance,BigDecimal creditBalance) {

        //5 rollback
        //0 deposit
        CartTransaction newCartTransaction = performTransaction(processRequest, transactionType, majorType,creditBalance,realBalance);
        return transactionRepository.save(newCartTransaction);
    }


    private CartTransaction updateTransaction(CartTransaction oldTransaction, Long newTransactionType, Long majorType) {
        CartTransaction cartTransaction = new CartTransaction();
        cartTransaction.setRrn(oldTransaction.getRrn());
        //---- به روز رسانی ---
        cartTransaction.setTransactionType(newTransactionType);
        cartTransaction.setMajorType(majorType);
        //---------------

        cartTransaction.setCreditUsed(oldTransaction.getCreditUsed());
        cartTransaction.setCurrency(oldTransaction.getCurrency());
        cartTransaction.setRealUsed(oldTransaction.getRealUsed());
        cartTransaction.setStan(oldTransaction.getStan());
        cartTransaction.setCartId(oldTransaction.getCartId());
        cartTransaction.setAccountNumber(oldTransaction.getAccountNumber());
        cartTransaction.setAuthCode(oldTransaction.getAuthCode());
        cartTransaction.setMcc(oldTransaction.getMcc());
        cartTransaction.setMerchantId(oldTransaction.getMerchantId());
        cartTransaction.setTerminalId(oldTransaction.getTerminalId());
        cartTransaction.setStatus(oldTransaction.getStatus()); //TODO دارم همه ی استاتوس ها رو success میزنم
        cartTransaction.setCreatedAt(LocalDateTime.now()); // TODO به نظرم بتره Instant باشه

        return cartTransaction;
    }

    private CartTransaction performTransaction(ProcessRequest request, Long newTransactionType, Long majorType, BigDecimal realBalance, BigDecimal creditBalance) {
        CartTransaction cartTransaction = new CartTransaction();
        cartTransaction.setRrn(request.rrn());
        //---- به روز رسانی ---
        cartTransaction.setTransactionType(newTransactionType);
        cartTransaction.setMajorType(majorType);
        //---------------


        cartTransaction.setCurrency(request.currency());
        //--------------  برای تمام حالا قابل در نظر گرفتن --------------
        cartTransaction.setRealUsed(realBalance);
        cartTransaction.setCreditUsed(creditBalance);

        cartTransaction.setAmount(request.amount());
        cartTransaction.setCartId(request.cartId());
        cartTransaction.setAuthCode(GeneralUtils.generateAuthCodeAlphaNumeric());
        cartTransaction.setStan(Long.valueOf(GeneralUtils.generateStan()));
        cartTransaction.setMcc(request.mcc());
        cartTransaction.setMerchantId(request.merchantId());
        cartTransaction.setTerminalId(request.terminalId());
        cartTransaction.setStatus("SUCCESS"); //TODO دارم همه ی استاتوس ها رو success میزنم
        cartTransaction.setCreatedAt(LocalDateTime.now()); // TODO به نظرم بتره Instant باشه

        return cartTransaction;
    }

}
