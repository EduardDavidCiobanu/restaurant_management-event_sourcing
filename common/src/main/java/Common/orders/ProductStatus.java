package Common.orders;

/**
 * author Ciobanu Eduard David
 */
public enum ProductStatus {
    NEW,
    UPDATED,
    DELETED,
    PREPARED,
    REJECTED;

    public static ProductStatus map(String status) {
        switch (status) {
            case "NEW":
                return NEW;
            case "UPDATED":
                return UPDATED;
            case "DELETED":
                return DELETED;
            case "PREPARED":
                return PREPARED;
            default:
                return REJECTED;
        }
    }
}
