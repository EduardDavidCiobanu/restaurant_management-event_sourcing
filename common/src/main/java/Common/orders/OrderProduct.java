package Common.orders;

import org.bson.Document;

/**
 * author Ciobanu Eduard David
 */
public class OrderProduct {
    private int id;
    private int menuNumber;
    private String name;
    private int quantity;
    private String specs;
    private ProductStatus status;

    public OrderProduct(int id, int menuNumber, String name, int quantity, String specs, ProductStatus status) {
        this.id = id;
        this.menuNumber = menuNumber;
        this.name = name;
        this.quantity = quantity;
        this.specs = specs;
        this.status = status;
    }

    public OrderProduct(Document documentProduct) {
        this.id = documentProduct.getInteger("id");
        this.menuNumber = documentProduct.getInteger("menuNumber");
        this.name = documentProduct.getString("name");
        this.quantity = documentProduct.getInteger("quantity");
        this.specs = documentProduct.getString("specs");
        this.status = ProductStatus.map(documentProduct.getString("status"));
    }

    public int getId() {
        return id;
    }

    public int getMenuNumber() {
        return menuNumber;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSpecs() {
        return specs;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setPrepared(boolean prepared) {
        if (prepared)
            this.status = ProductStatus.PREPARED;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void update(int quantity, String specs) {
        this.quantity = quantity;
        this.specs = specs;
    }

    @Override
    public String toString() {
        return "Product[" + this.id + "]: " + this.name + ", status: " + this.status +
                ", menuNo: " + this.menuNumber +
                ", qty: " + this.quantity + ", specs: " + this.specs;
    }

    public Document toDocument() {
        return new Document().append("id", id)
                .append("menuNumber", menuNumber)
                .append("name", name)
                .append("quantity", quantity)
                .append("specs", specs)
                .append("status", status.toString());
    }
}
