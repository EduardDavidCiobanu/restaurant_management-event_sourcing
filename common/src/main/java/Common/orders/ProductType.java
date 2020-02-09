package Common.orders;

/**
 * author Ciobanu Eduard David
 */
public enum ProductType {
    MEAL,   // 0 - 149
    PIZZA,  // 150 - 199
    DRINK; // >= 200

    public static ProductType match(int code) {
        if (code < 149) {
            return MEAL;
        }
        if (code < 199) {
            return PIZZA;
        } else {
            return DRINK;
        }
    }

    public static ProductType match(String s) {
        switch (s) {
            case "PIZZA":
                return PIZZA;
            case "MEAL":
                return MEAL;
            default:
                return DRINK;
        }
    }
}
