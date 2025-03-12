package com.xj.payment_processor.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.apache.catalina.connector.Response;
import org.json.JSONObject;

import com.xj.payment_processor.config.ResponseStorage;
import com.xj.payment_processor.service.TransactionService;

@Service
public class TransactionConsumer {
    private final TransactionService transactionService;
    private final ResponseStorage responseStorage;

    public TransactionConsumer(TransactionService transactionService, ResponseStorage responseStorage) {
        this.transactionService = transactionService;
        this.responseStorage = responseStorage;
    }

    @KafkaListener(topics = "transaction-events", groupId = "payment-group")
    public void processTransaction(String message) {
        System.out.println("TransactionEventProcessor: Message received: " + message);
        
        JSONObject json = new JSONObject(message);
        Long transactionId = json.getLong("transactionId");
        Long senderId = json.getLong("senderId");
        Long receiverId = json.getLong("receiverId");
        Double amount = json.getDouble("amount");

        try {
            transactionService.processTransaction(transactionId, senderId, receiverId, amount);
            System.out.println("Transaction " + transactionId + " completed successfully.");
            
        } catch (Exception e) {
            System.out.println("Transaction " + transactionId + " failed: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "transaction-status-update", groupId = "payment-group")
    public void updateTransactionStatus(String message) {
        JSONObject json = new JSONObject(message);
        String transactionId = json.getString("transactionId");
        String status = json.getString("status");
        responseStorage.completeResponse(transactionId, status);
    }
}