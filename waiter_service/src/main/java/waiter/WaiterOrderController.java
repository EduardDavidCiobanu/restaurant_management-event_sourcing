package waiter;

import Common.Employee;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

@RestController
public class WaiterOrderController {

    private final WaiterService waiterService;

    public WaiterOrderController() throws Exception {
        String par1 = "";
        String par2 = "";

        File file = new File("SERVER_PROPERTIES.txt");
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] args = sc.nextLine().split(" ");
                if (args[0].equals("KAFKA"))
                    par1 = args[1];
                if (args[0].equals("MONGODB"))
                    par2 = args[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!par1.equals("") && !par2.equals(""))
            waiterService = new WaiterService(par1, par2);
        else
            throw new Exception("Parameters error!");
    }

    @GetMapping("/activeOrders/{waiterId}")
    String getActiveOrders(@PathVariable Integer waiterId) {
        return waiterService.getActiveOrders(waiterId);
    }

    @GetMapping("/preparedOrders/{waiterId}")
    String getPreparedOrders(@PathVariable Integer waiterId) {
        return waiterService.getPreparedOrders(waiterId);
    }

    @PostMapping("/newOrder")
    String postOrder(@RequestBody String newOrder) throws Exception {
        return waiterService.orderHandler(newOrder);
    }

    @PutMapping("/updateOrder")
    String updateOrder(@RequestBody String updateOrder) throws Exception {
        return waiterService.orderHandler(updateOrder);
    }

    @DeleteMapping("/deleteOrder")
    String deleteOrder(@RequestBody String deleteOrder) throws Exception {
        return waiterService.orderHandler(deleteOrder);
    }

    @GetMapping("/menu")
    List<Document> getMenu() {
        return waiterService.getMenu();
    }

    @GetMapping("/freeTables")
    List<Integer> getFreeTables() {
        return waiterService.getFreeTables();
    }

    @GetMapping("/login")
    public Employee login(@RequestParam("uname") String uname,
                          @RequestParam("pass") String pass) throws Exception {
        return this.waiterService.login(uname, pass);
    }

}
