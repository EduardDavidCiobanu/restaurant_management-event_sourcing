package Common.orders;

/**
 * author Ciobanu Eduard David
 */
public enum OrderType {
    GENERAL_ORDER,
    FINISHED_ORDER,
    UPDATE_ORDER,
    DELETE_ORDER,

    GENERAL_ONLINE_ORDER,
    DELIVERED_ONLINE_ORDER,
    UPDATE_ONLINE_ORDER,
    DELETE_ONLINE_ORDER,

    PIZZA_ORDERED,
    PIZZA_UPDATED,
    PIZZA_DELETED,
    PIZZA_PREPARED,

    MEAL_ORDERED,
    MEAL_UPDATED,
    MEAL_PREPARED,
    MEAL_DELETED,

    DRINK_ORDERED,
    DRINK_UPDATED,
    DRINK_PREPARED,
    DRINK_DELETED;

    /**
     * Returns categorized OrderType.
     * <p>Ex 1: GENERAL_ORDER, MEAL -> MEAL_ORDERED</p>
     * <p>Ex 2: DELETE_ORDER, PIZZA -> PIZZA_DELETED</p>
     * <p>Ex 3: UPDATE_ORDER, DRINK -> DRINK_ORDERED</p>=
     */
    public static OrderType getChildOrderType(OrderType parentOrderType, ProductType productType) {

        if (parentOrderType.equals(GENERAL_ORDER) || parentOrderType.equals(GENERAL_ONLINE_ORDER)) {

            switch (productType) {
                case MEAL:
                    return MEAL_ORDERED;
                case DRINK:
                    return DRINK_ORDERED;
                default:
                    return PIZZA_ORDERED;
            }
        }

        if (parentOrderType.equals(UPDATE_ORDER) || parentOrderType.equals(UPDATE_ONLINE_ORDER)) {

            switch (productType) {
                case MEAL:
                    return MEAL_UPDATED;
                case DRINK:
                    return DRINK_UPDATED;
                default:
                    return PIZZA_UPDATED;
            }
        }

        if (parentOrderType.equals(DELETE_ORDER) || parentOrderType.equals(DELETE_ONLINE_ORDER)
                || parentOrderType.equals(FINISHED_ORDER) || parentOrderType.equals(DELIVERED_ONLINE_ORDER)) {
            switch (productType) {
                case MEAL:
                    return MEAL_DELETED;
                case DRINK:
                    return DRINK_DELETED;
                default:
                    return PIZZA_DELETED;
            }
        }

        return null;
    }

    public static OrderType map(String stringOrderType) {
        switch (stringOrderType) {
            case "GENERAL_ORDER":
                return GENERAL_ORDER;
            case "FINISHED_ORDER":
                return FINISHED_ORDER;
            case "UPDATE_ORDER":
                return UPDATE_ORDER;
            case "DELETE_ORDER":
                return DELETE_ORDER;
            case "GENERAL_ONLINE_ORDER":
                return GENERAL_ONLINE_ORDER;
            case "DELIVERED_ONLINE_ORDER":
                return DELIVERED_ONLINE_ORDER;
            case "UPDATE_ONLINE_ORDER":
                return UPDATE_ONLINE_ORDER;
            case "DELETE_ONLINE_ORDER":
                return DELETE_ONLINE_ORDER;
            case "PIZZA_ORDERED":
                return PIZZA_ORDERED;
            case "PIZZA_UPDATED":
                return PIZZA_UPDATED;
            case "PIZZA_DELETED":
                return PIZZA_DELETED;
            case "PIZZA_PREPARED":
                return PIZZA_PREPARED;
            case "MEAL_ORDERED":
                return MEAL_ORDERED;
            case "MEAL_UPDATED":
                return MEAL_UPDATED;
            case "MEAL_PREPARED":
                return MEAL_PREPARED;
            case "MEAL_DELETED":
                return MEAL_DELETED;
            case "DRINK_ORDERED":
                return DRINK_ORDERED;
            case "DRINK_UPDATED":
                return DRINK_UPDATED;
            case "DRINK_PREPARED":
                return DRINK_PREPARED;
            case "DRINK_DELETED":
                return DRINK_DELETED;
            default:
                return null;
        }
    }
}
