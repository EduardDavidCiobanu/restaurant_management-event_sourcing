package Common.orders;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * author Ciobanu Eduard David
 */
public class Order {

    /* Properties */
    public final OrderType orderType;
    public final long orderDate;
    public Integer tableNumber;
    public final Integer orderId;
    public final Integer parentOrderId;
    public final Integer waiterId;

    public List<OrderProduct> products;

    // ********** Constructors Part **********
    public Order(OrderType orderType, long orderDate, int userId, int parentEventId, int tableNumber, List<OrderProduct> products) {
        this.products = products;
        this.orderType = orderType;
        this.orderDate = orderDate;
        this.waiterId = userId;
        this.orderId = (new Random()).nextInt(((2147483647 - 1) + 1)) + 1;
        this.parentOrderId = parentEventId;
        this.tableNumber = tableNumber;
    }

    public Order(OrderType orderType, Order oldOrder) throws Exception {
        if (oldOrder.products.size() == 0)
            throw new Exception("No products in the order!");

        this.products = oldOrder.products;
        this.orderType = orderType;
        this.orderDate = new Date().getTime();
        this.waiterId = oldOrder.waiterId;
        this.orderId = oldOrder.orderId;
        this.parentOrderId = 0;
        this.tableNumber = oldOrder.tableNumber;
    }

    public Order(Order copy) throws Exception {

        if (copy.tableNumber == null || copy.orderType == null || copy.waiterId == null ||
                copy.products == null || copy.orderId == null) {
            throw new Exception("Null value found in a variable!");
        }

        if (copy.products.size() == 0)
            throw new Exception("No products in the order!");

        this.products = copy.products;
        this.orderType = copy.orderType;
        this.orderDate = copy.orderDate;
        this.waiterId = copy.waiterId;
        this.orderId = copy.orderId;
        this.parentOrderId = copy.parentOrderId;
        this.tableNumber = copy.tableNumber;
    }

    public Order(Document documentOrder) {
        this.orderType = OrderType.map(documentOrder.getString("orderType"));
        this.orderDate = documentOrder.getLong("orderDate");
        this.tableNumber = documentOrder.getInteger("tableNumber");
        this.orderId = documentOrder.getInteger("orderId");
        this.parentOrderId = documentOrder.getInteger("parentOrderId");
        this.waiterId = documentOrder.getInteger("waiterId");

        this.products = new ArrayList<>();
        for (Document docProd : documentOrder.getList("products", Document.class)) {
            products.add(new OrderProduct(docProd));
        }
    }

    // ********** Methods Part **********
    public int getProductIndex(int prodId) {
        int i;
        for (i = 0; i < this.products.size(); i++) {
            if (this.products.get(i).getId() == prodId) {
                break;
            }
        }
        return i;
    }

    public long getOrderDate() {
        return orderDate;
    }

    @Override
    public String toString() {
        String result = "orderType: " + orderType + "; ";
        result += "orderId: " + orderId + "; ";
        result += "userId: " + waiterId + "; ";
        if (parentOrderId != 0)
            result += "parentOrderId: " + parentOrderId + "; ";
        result += "products: " + products.toString() + "; ";
        return result;
    }

    public Document toDocument() {
        Document documentOrder = new Document("orderType", orderType.toString())
                .append("orderDate", orderDate)
                .append("tableNumber", tableNumber)
                .append("orderId", orderId)
                .append("parentOrderId", parentOrderId)
                .append("waiterId", waiterId);

        List<Document> documentProducts = new ArrayList<>();
        for (OrderProduct product : products) {
            documentProducts.add(product.toDocument());
        }
        documentOrder.append("products", documentProducts);
        return documentOrder;
    }
}

