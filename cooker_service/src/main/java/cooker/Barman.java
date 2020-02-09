package cooker;

import Common.Employee;
import Common.kafka.KafkaTopic;
import Common.orders.Order;

import java.util.List;

class Barman extends Cooker {

    Barman(String kafkaServerPath, String mongodbPath) {
        super(kafkaServerPath, mongodbPath, KafkaTopic.DrinkOrders,
                "Barman", "comenzi_active_barmani");
    }

    Employee login(String account, String password) throws Exception {
        return this.restaurantDB.login(account, password);
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
                        case DRINK_ORDERED:
                            handleNewOrder(order);
                            break;
                        case DRINK_UPDATED:
                            handleUpdateOrder(order);
                            break;
                        case DRINK_DELETED:
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
