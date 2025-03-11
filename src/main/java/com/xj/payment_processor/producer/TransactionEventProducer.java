package com.xj.payment_processor.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

@Service
public class TransactionEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(Long transactionId, String senderId, String receiverId, String amount) {
        JSONObject event = new JSONObject();
        event.put("transactionId", transactionId);
        event.put("senderId", senderId);
        event.put("receiverId", receiverId);
        event.put("amount", amount);
        
        kafkaTemplate.send("transaction-events", event.toString());
    }
}