package cooker;

import Common.kafka.KafkaTopic;
import Common.orders.Order;

import java.util.List;

class GeneralCooker extends Cooker {

    GeneralCooker(String kafkaServerPath, String mongodbPath) {
        super(kafkaServerPath, mongodbPath, KafkaTopic.MealOrders,
                "GeneralCooker", "comenzi_active_bucatari");
    }

    @Override
    void handleReceivedOrders() {
        new Thread(() -> {
            while (true) {
                List<Order> receivedOrders = this.kafkaOrderReceiver.getOrders();
                System.out.println("\n");
                for (Order order : receivedOrders) {
                    // For KitchenServerChecker
                    this.setLastReceivedOrderDate(order.orderDate);

                    switch (order.orderType) {
                        case MEAL_ORDERED:
                            handleNewOrder(order);
                            break;
                        case MEAL_UPDATED:
                            handleUpdateOrder(order);
                            break;
                        case MEAL_DELETED:
                            handleDeleteOrder(order);
                            break;
                        default:
                            break;
                    }
                }
            }
        }).start();
    }
}
