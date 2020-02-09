package Common;

import com.mongodb.client.*;
import Common.orders.Order;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;
/**
 * author Ciobanu Eduard David
 */
public class RestaurantDatabase {

    private final MongoCollection<Document> empCollection;
    private final MongoCollection<Document> menuCollection;
    private final MongoCollection<Document> tablesCollection;
    private final MongoCollection<Document> activeOrdersCollection;
    private MongoCollection<Document> receiptsCollection;

    /**
     * @param connectionString path to the mongoDB; if local, path is: mongodb://localhost
     * @param AOCName ActiveOrdersCollection database name
     */
    public RestaurantDatabase(String connectionString, String AOCName){
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase db = mongoClient.getDatabase("Restaurant");
        this.empCollection = db.getCollection("utilizatori");
        this.menuCollection = db.getCollection("meniu");
        this.tablesCollection = db.getCollection("mese");
        this.activeOrdersCollection = db.getCollection(AOCName);
        this.receiptsCollection = db.getCollection("bonuri");
    }


    public Employee login(String userName, String password) throws Exception {
        if (userName == null || password == null){
            throw new Exception("Credentials contains null.");
        }
        Document query = new Document().append("account_name", userName)
                .append("password", password);
        Document res = this.empCollection.find(query).first();
        return new Employee(res);

    }
    public Employee getEmpInfo(int empId){
        Document query = new Document().append("emp_id", empId);
        try {
            return new Employee(empCollection.find(query).first());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Document> getMenu(){

        FindIterable<Document> it = this.menuCollection.find().projection(excludeId());
        ArrayList<Document> menuDocs = new ArrayList<>();

        for(Document its : it){
            menuDocs.add(its);
        }
        return menuDocs;
    }

    public List<Integer> getFreeTables(){
        Document query = new Document().append("status", Table.Status.FREE.toString());
        FindIterable<Document> result =  this.tablesCollection.find(query);

        List<Integer> freeTables = new ArrayList<>();
        for(Document doc : result){
            freeTables.add(doc.getInteger("number"));
        }

        return freeTables;
    }
    public boolean setTablesStatus(List<Integer> tableNumbers, Table.Status status){
        try {
            for(Integer number : tableNumbers){
                Document query = new Document().append("number", number);
                Document update = new Document().append("$set",
                                        new Document().append("status", status.toString()));
                this.tablesCollection.updateOne(query, update);
            }
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public void freeAllTables(){
        Document query = new Document().append("status", Table.Status.OCCUPIED.toString());
        Document update = new Document().append("$set",
                new Document().append("status", Table.Status.FREE.toString()));
        this.tablesCollection.updateMany(query, update);
    }

    public boolean saveReceipt(Document receipt){
        try {
            this.receiptsCollection.insertOne(receipt);
            return true;
        }
        catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean addNewOrder(Order order){
        try {
            this.activeOrdersCollection.insertOne(order.toDocument());
            return true;
        }
        catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public List<Order> getActiveOrders(Integer waiterId){
        FindIterable<Document> result;
        if(waiterId != null) {
            Document query = new Document().append("waiterId", waiterId);
            result = this.activeOrdersCollection.find(query);
        }
        else {
            result = this.activeOrdersCollection.find();
        }

        List<Order> activeOrders = new ArrayList<>();
        for(Document doc : result){
            activeOrders.add(new Order(doc));
        }

        return activeOrders;
    }
    public Order findAndDeleteOrder(String key, Integer orderId){
        Document query = new Document(key, orderId);
        Document docOrder = this.activeOrdersCollection.findOneAndDelete(query);
        if(docOrder != null)
            return new Order(docOrder);
        else
            return null;
    }
    public boolean updateOrder(Order order){
        try {
            this.activeOrdersCollection.insertOne(order.toDocument());
            return true;
        }
        catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public boolean deleteOrder(String key, Integer orderId){
        try {
            this.activeOrdersCollection.deleteOne(new Document(key, orderId));
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
}
