package cooker;

import Common.Employee;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@RestController
public class CookerOrderController {

    private final Barman barman;
    private final GeneralCooker generalCooker;
    private final PizzaCooker pizzaCooker;
    private final Gson jsonMapper;
    private final KitchenServiceChecker kitchenServiceChecker;

    public CookerOrderController() throws Exception {
        String kafkaPath = "";
        String mongoPath = "";

        File file = new File("SERVER_PROPERTIES.txt");
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] args = sc.nextLine().split(" ");
                if (args[0].equals("KAFKA"))
                    kafkaPath = args[1];
                if (args[0].equals("MONGODB"))
                    mongoPath = args[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (kafkaPath.equals("") && mongoPath.equals(""))
            throw new Exception("Parameters error!");

        System.out.println("[CookerService] Starting Barman service...");
        barman = new Barman(kafkaPath, mongoPath);

        System.out.println("[CookerService] Starting GeneralCooker service...");
        generalCooker = new GeneralCooker(kafkaPath, mongoPath);

        System.out.println("[CookerService] Starting PizzaCooker service...");
        pizzaCooker = new PizzaCooker(kafkaPath, mongoPath);

        kitchenServiceChecker = new KitchenServiceChecker(kafkaPath, generalCooker);
        jsonMapper = new Gson();
    }

    @GetMapping("/generalCooker/checkKitchenService")
    public String getKitchenServiceState(){
        return kitchenServiceChecker.getState();
    }

    @GetMapping("/login")
    public Employee login(@RequestParam("uname") String uname,
                          @RequestParam("pass") String pass) throws Exception {
        return this.barman.login(uname, pass);
    }

    @GetMapping("/barman/activeOrders")
    String getBarmanActiveOrders() {
        return jsonMapper.toJson(barman.getActiveOrders());
    }

    @GetMapping("/pizzaCooker/activeOrders")
    String getPizzaActiveOrders() {
        return jsonMapper.toJson(pizzaCooker.getActiveOrders());
    }

    @GetMapping("/generalCooker/activeOrders")
    String getGeneralActiveOrders() {
        return jsonMapper.toJson(generalCooker.getActiveOrders());
    }

    @PostMapping("/barman/preparedOrder")
    String drinkPrepared(@RequestBody String newOrder) {
        return barman.sendPreparedOrder(newOrder);
    }

    @PostMapping("/pizzaCooker/preparedOrder")
    String pizzaPrepared(@RequestBody String newOrder) {
        return pizzaCooker.sendPreparedOrder(newOrder);
    }

    @PostMapping("/generalCooker/preparedOrder")
    String mealPrepared(@RequestBody String newOrder) {
        return generalCooker.sendPreparedOrder(newOrder);
    }

}
