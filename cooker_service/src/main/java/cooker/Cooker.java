package cooker;

import com.google.gson.Gson;
import Common.RestaurantDatabase;
import Common.kafka.KafkaReceiver;
import Common.kafka.KafkaSender;
import Common.kafka.KafkaTopic;
import Common.orders.Order;
import Common.orders.OrderProduct;
import Common.orders.ProductStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

abstract class Cooker {

    // Fields
    private KafkaSender kafkaOrderSender;
    KafkaReceiver kafkaOrderReceiver;
    RestaurantDatabase restaurantDB;
    private Gson jsonMapper;

    private static long lastReceivedOrderDate = 0;
    private final Object lock = new Object();

    void setLastReceivedOrderDate(long date){
        synchronized (lock){
            lastReceivedOrderDate = date;
        }
    }

    long getLastReceivedOrderDate(){
        synchronized (lock){
            return lastReceivedOrderDate;
        }
    }

    /**
     * @param AOCName Active Orders mongoDB Collection name
     */
    Cooker(String kafkaServerPath, String mongodbPath, KafkaTopic kafkaReadTopic, String cookerId, String AOCName) {

        this.restaurantDB = new RestaurantDatabase(mongodbPath, AOCName);
        this.kafkaOrderSender = new KafkaSender(kafkaServerPath);
        this.kafkaOrderReceiver = new KafkaReceiver(kafkaServerPath, kafkaReadTopic, cookerId);
        this.jsonMapper = new Gson();
        this.handleReceivedOrders();
    }

    // ************ Methods ************
    List<Order> getActiveOrders() {
        List<Order> orders = this.restaurantDB.getActiveOrders(null);
        orders.sort(Comparator.comparing(Order::getOrderDate));
        return orders;
    }

    boolean compareOrders(Order o1, Order o2) {
        if (o1.orderDate < o2.orderDate)
            return true;
        else
            return false;
    }

    String sendPreparedOrder(String jsonOrder) {
        Order preparedOrder = jsonMapper.fromJson(jsonOrder, Order.class);
        this.kafkaOrderSender.sendOrder(preparedOrder, KafkaTopic.PreparedOrders);
        System.out.println("\n");

        setPreparedProducts(preparedOrder);

        return preparedOrder.orderType + " [" + preparedOrder.orderId + "]" +
                " has been successfully sent.";
    }

    private void setPreparedProducts(Order preparedOrder) {
        // PreparedOrder has the id of the initial GeneralOrder id in parentOrderId
        Order updatedOrder = this.restaurantDB.findAndDeleteOrder("parentOrderId", preparedOrder.parentOrderId);
        for (OrderProduct product : preparedOrder.products) {
            int index = updatedOrder.getProductIndex(product.getId());
            updatedOrder.products.get(index).setStatus(ProductStatus.PREPARED);
        }
        this.restaurantDB.updateOrder(updatedOrder);
    }

    abstract void handleReceivedOrders();

    void handleNewOrder(Order order) {
        this.restaurantDB.addNewOrder(order);
    }

    void handleUpdateOrder(Order order) {
        Order updatedOrder = this.restaurantDB.findAndDeleteOrder("parentOrderId", order.parentOrderId);
        if (updatedOrder == null) {
            this.restaurantDB.addNewOrder(order);
            return;
        }
//        updatedOrder.waiterId = order.waiterId;
        if (!order.tableNumber.equals(updatedOrder.tableNumber))
            updatedOrder.tableNumber = order.tableNumber;

        boolean hasNewProd = false;

        for (OrderProduct product : order.products) {
            int prodId;
            switch (product.getStatus()) {
                case NEW:
                    updatedOrder.products.add(product);
                    hasNewProd = true;
                    break;
                case UPDATED:
                    prodId = updatedOrder.getProductIndex(product.getId());
                    updatedOrder.products.get(prodId).update(product.getQuantity(), product.getSpecs());
                    break;
                case DELETED:
                    prodId = updatedOrder.getProductIndex(product.getId());
                    updatedOrder.products.remove(prodId);
                    break;
            }
        }

        if (hasNewProd) {
            updatedOrder = new Order(updatedOrder.orderType, order.orderDate, order.waiterId,
                    updatedOrder.parentOrderId, updatedOrder.tableNumber, updatedOrder.products);
        }
        this.restaurantDB.updateOrder(updatedOrder);
    }

    void handleDeleteOrder(Order order) {
        this.restaurantDB.deleteOrder("parentOrderId", order.parentOrderId);
    }
}
