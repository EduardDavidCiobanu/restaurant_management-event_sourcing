package Common.kafka;

import com.google.gson.Gson;
import Common.orders.Order;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class KafkaReceiver {
    private Gson jsonMapper;
    private KafkaConsumer<String, String> consumer;

    public KafkaReceiver(String serverPath, KafkaTopic consumerTopicName, String consID) {
        this.jsonMapper = new Gson();

        Properties consProps = new Properties();
        consProps.put("bootstrap.servers", serverPath);
        consProps.put("group.id", consID);
        consProps.put("enable.auto.commit", "true");
        consProps.put("auto.commit.interval.ms", "1000");
        consProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        this.consumer = new KafkaConsumer<>(consProps);
        this.consumer.subscribe(Collections.singletonList(consumerTopicName.toString()));
    }

    public List<Order> getOrders() {
        while (true) {
            ConsumerRecords<String, String> jsonOrders = consumer.poll(Duration.ofSeconds(100));
            if (!jsonOrders.isEmpty()) {

                List<Order> orders = new ArrayList<>();
                for (ConsumerRecord<String, String> jsonOrder : jsonOrders) {
                    orders.add(jsonMapper.fromJson(jsonOrder.value(), Order.class));
                    System.out.println("Kafka received: " + jsonOrder.value());
                }

                return orders;
            }
        }
    }
}
