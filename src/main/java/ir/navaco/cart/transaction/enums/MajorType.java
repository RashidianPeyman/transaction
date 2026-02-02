package ir.navaco.cart.transaction.enums;

public enum MajorType {
    DEPOSIT(0, "deposit"),
    DEBIT(1, "debit");
    private final int code;
    private final String title;

    MajorType(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static TransactionType fromCode(int code) {

    }
}
