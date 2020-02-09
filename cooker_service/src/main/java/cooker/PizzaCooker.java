package cooker;

import Common.kafka.KafkaTopic;
import Common.orders.Order;

import java.util.List;

class PizzaCooker extends Cooker {

    PizzaCooker(String kafkaServerPath, String mongodbPath) {
        super(kafkaServerPath, mongodbPath, KafkaTopic.PizzaOrders,
                "PizzaCooker", "comenzi_active_pizzetari");
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
                        case PIZZA_ORDERED:
                            handleNewOrder(order);
                            break;
                        case PIZZA_UPDATED:
                            handleUpdateOrder(order);
                            break;
                        case PIZZA_DELETED:
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
