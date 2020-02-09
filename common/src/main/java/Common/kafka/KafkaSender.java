package Common.kafka;

import com.google.gson.Gson;
import Common.orders.Order;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaSender {
    private KafkaProducer<String, String> producer;
    private Gson jsonMapper;

    public KafkaSender(String serverPath) {
        this.jsonMapper = new Gson();

        Properties prodProps = new Properties();
        prodProps.put("bootstrap.servers", serverPath);
        prodProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prodProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(prodProps);
    }

    public void sendOrder(Order order, KafkaTopic topicName) {
        String jsonOrder = jsonMapper.toJson(order);
        this.producer.send(new ProducerRecord<>(topicName.toString(), jsonOrder));
        System.out.println("Kafka send: " + jsonOrder.toString());
    }
}
