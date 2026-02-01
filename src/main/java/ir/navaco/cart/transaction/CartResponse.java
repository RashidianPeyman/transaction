package ir.navaco.cart.transaction;


import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartResponse {
    private final String transactionId;   // کد پیگیری داخلی ما
    private final Long CartId;          // شناسه کیف پول
    private final BigDecimal currentBalance; // موجودی واقعی (بدون پول‌های بلوکه شده)
    private final BigDecimal availableBalance; // موجودی قابل برداشت
    private final String status;          // وضعیت تراکنش (مثلاً PENDING یا SUCCESS)
    private final String message;         // پیام برای نمایش به کاربر

    // متد کمکی برای ساخت پاسخ‌های موفق
    public static CartResponse success(String txId, Cart cart) {
        return new CartResponse(
                txId,
                cart.getId(),
                cart.getBalance().add(cart.getBlockedBalance()), // کل دارایی
                cart.getBalance(),                                // آنچه واقعاً می‌تواند خرج کند
                "OK",
                "تراکنش با موفقیت رزرو شد"
        );
    }
}