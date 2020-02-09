package kitchen;

import Common.kafka.KafkaReceiver;
import Common.kafka.KafkaSender;
import Common.kafka.KafkaTopic;
import Common.orders.Order;
import Common.orders.OrderProduct;
import Common.orders.OrderType;
import Common.orders.ProductType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class KitchenService {

    private KafkaReceiver kafkaOrderReceiver;
    private KafkaSender kafkaOrderSender;

    public KitchenService(String kafkaServerPath) {
        this.kafkaOrderReceiver = new KafkaReceiver(kafkaServerPath, KafkaTopic.GeneralOrders, "kitchenService");
        this.kafkaOrderSender = new KafkaSender(kafkaServerPath);
        this.listenForOrders();
    }

    // ********** Sending Orders Part **********

    /**
     * Post the newly created category orders to Kafka,
     * using the sender class.
     */
    private void sendCategorizedOrders(List<Order> categoryOrders) {
        for (Order order : categoryOrders) {
            switch (order.orderType) {
                case MEAL_ORDERED:
                case MEAL_UPDATED:
                case MEAL_PREPARED:
                case MEAL_DELETED:
                    this.kafkaOrderSender.sendOrder(order, KafkaTopic.MealOrders);
                    break;
                case PIZZA_ORDERED:
                case PIZZA_UPDATED:
                case PIZZA_PREPARED:
                case PIZZA_DELETED:
                    this.kafkaOrderSender.sendOrder(order, KafkaTopic.PizzaOrders);
                    break;
                case DRINK_ORDERED:
                case DRINK_UPDATED:
                case DRINK_PREPARED:
                case DRINK_DELETED:
                    this.kafkaOrderSender.sendOrder(order, KafkaTopic.DrinkOrders);
            }
            System.out.println("\n");
        }
    }

    /**
     * Breaks one general order into specific orders like
     * MEAL_ORDER or PIZZA_ORDER, and the returns a list with the category
     * orders created.
     */
    private List<Order> getCategorizedOrders(Order parentOrder) {
        List<Order> categorizedOrders = new ArrayList<>();
        Map<ProductType, List<OrderProduct>> productMap = new HashMap<>();

        sortProducts(productMap, parentOrder);

        for (Map.Entry<ProductType, List<OrderProduct>> listEntry : productMap.entrySet()) {
            if (isNewOrder(parentOrder)) {
                // New orders take the orderId from the general order
                OrderType childOrderType = OrderType.getChildOrderType(parentOrder.orderType, listEntry.getKey());
                categorizedOrders.add(new Order(childOrderType, parentOrder.orderDate, parentOrder.waiterId,
                        parentOrder.orderId, parentOrder.tableNumber, listEntry.getValue()));
            } else {
                OrderType childOrderType = OrderType.getChildOrderType(parentOrder.orderType, listEntry.getKey());
                categorizedOrders.add(new Order(childOrderType, parentOrder.orderDate, parentOrder.waiterId,
                        parentOrder.parentOrderId, parentOrder.tableNumber, listEntry.getValue()));
            }
        }

        return categorizedOrders;
    }

    private void sortProducts(Map<ProductType, List<OrderProduct>> productMap, Order order) {
        for (OrderProduct product : order.products) {
            ProductType productType = ProductType.match(product.getMenuNumber());

            if (productMap.containsKey(productType)) {
                productMap.get(productType).add(product);
            } else {
                productMap.put(productType, new ArrayList<>());
                productMap.get(productType).add(product);
            }
        }
    }

    private boolean isNewOrder(Order order) {
        return order.orderType.equals(OrderType.GENERAL_ONLINE_ORDER) || order.orderType.equals(OrderType.GENERAL_ORDER);
    }

    // ********** Receiving Orders Part **********

    /**
     * Handles the general orders received from Kafka list of orders
     * received from Kafka. The list of orders is then divided into category
     * orders and distributed to the specialized cookers.
     *
     * @param orders list of orders from WaiterService
     */
    private void ordersHandler(List<Order> orders) {
        System.out.println("\n");
        for (Order order : orders) {
            sendCategorizedOrders(getCategorizedOrders(order));
        }
    }

    private void listenForOrders() {
        while (true) {
            ordersHandler(this.kafkaOrderReceiver.getOrders());
        }
    }

    // ********** Public Methods **********

    public static void main(String[] args) throws Exception {

        String par1 = "";

        File file = new File("SERVER_PROPERTIES.txt");
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] arguments = sc.nextLine().split(" ");
                if (arguments[0].equals("KAFKA"))
                    par1 = arguments[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!par1.equals(""))
            new KitchenService(par1);
        else
            throw new Exception("Parameters error!");

    }
}


