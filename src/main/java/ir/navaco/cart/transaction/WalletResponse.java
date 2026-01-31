package ir.navaco.cart.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WalletResponse {
    private final String transactionId;   // کد پیگیری داخلی ما
    private final Long walletId;          // شناسه کیف پول
    private final BigDecimal currentBalance; // موجودی واقعی (بدون پول‌های بلوکه شده)
    private final BigDecimal availableBalance; // موجودی قابل برداشت
    private final String status;          // وضعیت تراکنش (مثلاً PENDING یا SUCCESS)
    private final String message;         // پیام برای نمایش به کاربر

    // متد کمکی برای ساخت پاسخ‌های موفق
    public static WalletResponse success(String txId, Wallet wallet) {
        return new WalletResponse(
                txId,
                wallet.getId(),
                wallet.getBalance().add(wallet.getBlockedBalance()), // کل دارایی
                wallet.getBalance(),                                // آنچه واقعاً می‌تواند خرج کند
                "OK",
                "تراکنش با موفقیت رزرو شد"
        );
    }
}