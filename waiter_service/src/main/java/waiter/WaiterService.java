package waiter;

import Common.Employee;
import Common.RestaurantDatabase;
import Common.Table;
import Common.kafka.KafkaReceiver;
import Common.kafka.KafkaSender;
import Common.kafka.KafkaTopic;
import Common.orders.Order;
import Common.orders.OrderProduct;
import Common.orders.OrderType;
import Common.orders.ProductStatus;
import com.google.gson.Gson;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


class WaiterService {

    private KafkaReceiver kafkaOrderReceiver;
    private KafkaSender kafkaOrderSender;
    private CopyOnWriteArrayList<Order> preparedOrders;
    private Gson jsonMapper;
    private RestaurantDatabase restaurantDB;
    private ReceiptMaker receiptMaker;

    WaiterService(String kafkaServerPath, String mongodbPath) {
        // "localhost:9092"
        this.kafkaOrderReceiver = new KafkaReceiver(kafkaServerPath, KafkaTopic.PreparedOrders, "waiterService");
        this.kafkaOrderSender = new KafkaSender(kafkaServerPath);
        this.preparedOrders = new CopyOnWriteArrayList<>();
        this.jsonMapper = new Gson();

        // "mongodb://localhost"
        this.restaurantDB = new RestaurantDatabase(mongodbPath, "comenzi_active_chelneri");
        this.receiptMaker = new ReceiptMaker(this.getMenu());

        handlePreparedOrders();
    }

    // ********** Methods **********
    Employee login(String account, String password) throws Exception {
        return this.restaurantDB.login(account, password);
    }

    List<Document> getMenu() {
        return this.restaurantDB.getMenu();
    }

    List<Integer> getFreeTables() {
        return this.restaurantDB.getFreeTables();
    }

    String getPreparedOrders(Integer waiterId) {

        List<Order> waiterPreparedOrders = new ArrayList<>();
        for (Order order : this.preparedOrders) {
            if (order.waiterId.equals(waiterId))
                waiterPreparedOrders.add(order);
        }

        boolean test = true;
        while (test) {
            test = false;
            int idx;
            for (idx = 0; idx < this.preparedOrders.size(); idx++) {
                if (this.preparedOrders.get(idx).waiterId.equals(waiterId)) {
                    test = true;
                    this.preparedOrders.remove(idx);
                    break;
                }
            }
        }
        return jsonMapper.toJson(waiterPreparedOrders);
    }

    String getActiveOrders(Integer waiterId) {
        return jsonMapper.toJson(this.restaurantDB.getActiveOrders(waiterId));
    }

    //     Methods for new orders
    String orderHandler(String receivedJsonOrder) throws Exception {

        Order receivedOrder = new Order(jsonMapper.fromJson(receivedJsonOrder, Order.class));

        if (receivedOrder.orderType.equals(OrderType.UPDATE_ONLINE_ORDER) || receivedOrder.orderType.equals(OrderType.UPDATE_ORDER)) {
            return sendUpdateOrder(receivedOrder);
        }
        if (receivedOrder.orderType.equals(OrderType.DELETE_ONLINE_ORDER) || receivedOrder.orderType.equals(OrderType.DELETE_ORDER)) {
            return sendDeleteOrder(receivedOrder);
        }
        if (receivedOrder.orderType.equals(OrderType.FINISHED_ORDER) || receivedOrder.orderType.equals(OrderType.DELIVERED_ONLINE_ORDER)) {
            return sendFinishedOrder(receivedOrder);
        }
        return sendRegularOrder(receivedOrder);
    }

    private String sendRegularOrder(Order order) {
        this.restaurantDB.setTablesStatus(Collections.singletonList(order.tableNumber), Table.Status.OCCUPIED);

        this.kafkaOrderSender.sendOrder(order, KafkaTopic.GeneralOrders);
        System.out.println("\n");
        this.restaurantDB.addNewOrder(order);

        return "Order with id[" + order.orderId + "] posted successfully!\n";
    }

    private String sendDeleteOrder(Order order) {

        this.restaurantDB.setTablesStatus(Collections.singletonList(order.tableNumber), Table.Status.FREE);
        this.restaurantDB.deleteOrder("orderId", order.parentOrderId);

        this.kafkaOrderSender.sendOrder(order, KafkaTopic.GeneralOrders);
        System.out.println("\n");
        return "Order with id[" + order.orderId + "] deleted successfully!\n";
    }

    private String sendFinishedOrder(Order order) {
        this.restaurantDB.setTablesStatus(Collections.singletonList(order.tableNumber), Table.Status.FREE);
        this.kafkaOrderSender.sendOrder(order, KafkaTopic.FinishedOrders);
        System.out.println("\n");

        this.restaurantDB.deleteOrder("orderId", order.parentOrderId);

        removeRejectedProducts(order);
        this.kafkaOrderSender.sendOrder(order, KafkaTopic.GeneralOrders);

        Document receipt = this.receiptMaker.makeReceipt(order, this.restaurantDB.getEmpInfo(order.waiterId));
        this.restaurantDB.saveReceipt(receipt);
        return jsonMapper.toJson(receipt);
    }

    private String sendUpdateOrder(Order updateOrder) {
        Order order = this.restaurantDB.findAndDeleteOrder("orderId", updateOrder.parentOrderId);
        updateOrderProducts(order, updateOrder);
        if (order.products.size() == 0) {
            try {
                return this.sendDeleteOrder(
                        new Order(OrderType.FINISHED_ORDER, updateOrder.orderDate, order.waiterId,
                                order.orderId, order.tableNumber, updateOrder.products));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.restaurantDB.updateOrder(order);
        // Rejected products are not sent to cookers;
        removeRejectedProducts(updateOrder);

        this.kafkaOrderSender.sendOrder(updateOrder, KafkaTopic.GeneralOrders);
        System.out.println("\n");
        return "Order[" + updateOrder.orderId + "] was updated successfully!";
    }

    private void updateOrderProducts(Order oldOrder, Order updateOrder) {
        if (!oldOrder.tableNumber.equals(updateOrder.tableNumber)) {
            this.restaurantDB.setTablesStatus(Collections.singletonList(oldOrder.tableNumber), Table.Status.FREE);
            this.restaurantDB.setTablesStatus(Collections.singletonList(updateOrder.tableNumber), Table.Status.OCCUPIED);
            oldOrder.tableNumber = updateOrder.tableNumber;
        }

        for (OrderProduct product : updateOrder.products) {
            int i = oldOrder.getProductIndex(product.getId());
            switch (product.getStatus()) {
                case NEW:
                    oldOrder.products.add(product);
                    break;
                case DELETED:
                    oldOrder.products.remove(i);
                    break;
                case UPDATED:
                    OrderProduct oldProduct = oldOrder.products.remove(i);
                    OrderProduct newProduct = new OrderProduct(oldProduct.getId(), oldProduct.getMenuNumber(),
                            oldProduct.getName(), product.getQuantity(), product.getSpecs(),
                            product.getStatus());
                    oldOrder.products.add(newProduct);
                    break;
                case REJECTED:
                    oldOrder.products.get(i).setStatus(ProductStatus.REJECTED);
                    break;
                default:
                    break;
            }
        }
    }

    private void removeRejectedProducts(Order order) {
        boolean loop = true;
        while (loop) {
            loop = false;
            for (int i = 0; i < order.products.size(); i++) {
                if (order.products.get(i).getStatus() == ProductStatus.REJECTED) {
                    order.products.remove(i);
                    loop = true;
                    break;
                }
            }
        }
    }

    //     Methods for prepared orders
    private void handlePreparedOrders() {
        new Thread(() -> {
            while (true) {
                List<Order> preparedOrders = kafkaOrderReceiver.getOrders();
                if (!preparedOrders.isEmpty()) {
                    System.out.println("\n");
                    savePreparedOrders(preparedOrders);
                }
            }
        }).start();
    }

    private void savePreparedOrders(List<Order> orders) {
        this.preparedOrders.addAll(orders);

        for (Order order : orders) {
            try {
                Order updatedOrder = this.restaurantDB.findAndDeleteOrder("orderId", order.parentOrderId);
                if (updatedOrder != null) {
                    for (int i = 0; i < updatedOrder.products.size(); i++) {
                        if (order.products.get(0).getId() == updatedOrder.products.get(i).getId()) {
                            updatedOrder.products.get(i).setPrepared(true);
                            break;
                        }
                    }
                    this.restaurantDB.updateOrder(updatedOrder);
                } else throw new Exception("this.restaurantDB.findAndDeleteOrder returned null!");
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private void generateReceipts(int year, int month, int days) {

        int day = 1;
        int hour;
        int opd; // Orders Per Day
        Random random = new Random();

        while (day <= days) {
            opd = 10 + random.nextInt(20);
            System.out.println("[" + year + "." + month + "." + day + "] - " + opd + " orders to generate.");
            while (opd > 0) {
                hour = 8 + random.nextInt(15);
                this.restaurantDB.saveReceipt(receiptMaker.generatedReceipt(random, 2 + random.nextInt(3),
                        year, month, day, hour));
                opd--;
            }
            day++;
        }

        System.out.println("Finished generating for " + year + "." + month);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WaiterService ws = new WaiterService("192.168.0.100:9092", "mongodb://192.168.0.100");
        ws.generateReceipts(2019, 10, 30);
        ws.generateReceipts(2019, 11, 31);
    }
}


