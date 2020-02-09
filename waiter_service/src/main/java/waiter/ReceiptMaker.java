package waiter;

import Common.Employee;
import Common.orders.Order;
import Common.orders.OrderProduct;
import Common.orders.ProductStatus;
import org.bson.Document;

import java.awt.geom.GeneralPath;
import java.util.*;

class ReceiptMaker {

    private Map<Integer, Double> productPrices;
    private Map<Integer, String> productNames;

    private ArrayList<Integer> productNumbers;

    ReceiptMaker(List<Document> menu) {
        productPrices = new HashMap<>();
        productNames = new HashMap<>();
        productNumbers = new ArrayList<>();
        for (Document doc : menu) {
            final int nr = doc.getInteger("nr");
            productNames.put(nr, doc.getString("name"));
            productPrices.put(nr, doc.getDouble("price"));

            productNumbers.add(nr);
        }
    }

    Document makeReceipt(Order order, Employee waiter) {
        Document receipt = new Document();

        receipt.append("date", new Date(order.orderDate));
        receipt.append("order_id", order.parentOrderId);
        receipt.append("waiter_name", waiter.getName());
        List<Document> products = new ArrayList<>();

        double total = 0.0;

        for (OrderProduct product : order.products) {
            if (product.getStatus() != ProductStatus.REJECTED) {
                final int nr = product.getMenuNumber();
                final double qty = (double) product.getQuantity();
                products.add(new Document()
                        .append("name", productNames.get(nr) + " X " + qty)
                        .append("price", productPrices.get(nr))
                        .append("total", productPrices.get(nr) * qty)
                );

                total += (productPrices.get(nr) * qty);
            }
        }
        receipt.append("products", products);
        receipt.append("total", total);
        return receipt;
    }

    Document generatedReceipt(Random random, int noOfProd, int year, int month, int day, int hour) {
        Document receipt = new Document();
        receipt.append("date", new GregorianCalendar(year, month, day, hour, 0).getTime());
        receipt.append("order_id", 0);
        receipt.append("waiter_name", "Generated");
        List<Document> products = new ArrayList<>();

        double total = 0.0;
        for (int i = 0; i < noOfProd; i++) {
            int prodIndex = random.nextInt(productNumbers.size() - 1);
            int nr = productNumbers.get(prodIndex);
            products.add(new Document()
                    .append("name", productNames.get(nr) + " X " + 1)
                    .append("price", productPrices.get(nr))
                    .append("total", productPrices.get(nr))
            );

            total += productPrices.get(nr);
        }

        receipt.append("products", products);
        receipt.append("total", total);


        return receipt;
    }
}
