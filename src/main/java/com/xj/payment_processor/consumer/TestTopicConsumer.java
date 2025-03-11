package com.xj.payment_processor.consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TestTopicConsumer {
    @KafkaListener(topics = "test-topic")
    public void listen(String message) {
        System.out.println("====================================");
        System.out.println("KAFKA MESSAGE CONSUMED: " + message);
        System.out.println("====================================");
    }
}
