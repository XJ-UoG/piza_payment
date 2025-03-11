package com.xj.payment_processor.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.json.JSONObject;
import com.xj.payment_processor.repository.UserRepository;
import com.xj.payment_processor.repository.TransactionRepository;
import com.xj.payment_processor.model.User;
import com.xj.payment_processor.model.Transaction;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionEventProcessor {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public TransactionEventProcessor(UserRepository userRepository,
                              TransactionRepository transactionRepository,
                              RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    @KafkaListener(topics = "transaction-events", groupId = "payment-group")
    public void processTransaction(String message) {
        System.out.println("TransactionEventProcessor: Message received: " + message);
        
        JSONObject json = new JSONObject(message);
        Long transactionId = json.getLong("transactionId");
        Long senderId = json.getLong("senderId");
        Long receiverId = json.getLong("receiverId");
        Double amount = json.getDouble("amount");

        try {
            // Fetch users and transaction
            User sender = userRepository.findById(senderId).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElseThrow();
            Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();

            // Check balance
            if (sender.getBalance() < amount) {
                throw new IllegalStateException();
            }

            // Process transfer
            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);
            
            userRepository.save(sender);
            userRepository.save(receiver);

            // Update Redis cache
            redisTemplate.opsForValue().set("user_balance:" + senderId, 
                String.valueOf(sender.getBalance()), 5, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("user_balance:" + receiverId, 
                String.valueOf(receiver.getBalance()), 5, TimeUnit.MINUTES);

            // Update transaction status
            transaction.setStatus("COMPLETED");
            transactionRepository.save(transaction);

        } catch (Exception e) {
            Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
        }
    }
}