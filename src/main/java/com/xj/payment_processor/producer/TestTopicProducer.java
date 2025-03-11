package com.xj.payment_processor.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestTopicProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TestTopicProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        System.out.println("Trying to send Kafka Message");
        kafkaTemplate.send("test-topic", message);
    }
}
