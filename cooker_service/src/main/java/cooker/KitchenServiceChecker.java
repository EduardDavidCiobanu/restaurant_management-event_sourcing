package cooker;

import Common.kafka.KafkaReceiver;
import Common.kafka.KafkaTopic;
import Common.orders.Order;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class KitchenServiceChecker {
    private enum CheckState {
        DONE,
        FAILED
    }

    private CheckState state;
    private final Object lock = new Object();
    private KafkaReceiver kafkaReceiver;
    private Cooker cooker;

    public KitchenServiceChecker(String kafkaServerPath, Cooker cooker) {
        this.kafkaReceiver = new KafkaReceiver(kafkaServerPath, KafkaTopic.GeneralOrders,
                "kitchenServiceChecker");
        this.state = CheckState.DONE;
        this.cooker = cooker;

        check();
    }

    private void setState(CheckState state) {
        synchronized (lock) {
            this.state = state;
        }
    }

    String getState() {
        synchronized (lock) {
            return this.state.toString();
        }
    }

    // Business
    private void check() {
        new Thread(() -> {
            while (true) {
                List<Order> receivedOrders = this.kafkaReceiver.getOrders();
                System.out.println("\n");

                if (!receivedOrders.isEmpty()){
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        if (cooker.getLastReceivedOrderDate() < receivedOrders.get(0).orderDate){
                            setState(CheckState.FAILED);

                            // Stay here until KitchenService is resolved
                            while (cooker.getLastReceivedOrderDate() < receivedOrders.get(0).orderDate){
                                TimeUnit.SECONDS.sleep(3);
                            }

                            setState(CheckState.DONE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
