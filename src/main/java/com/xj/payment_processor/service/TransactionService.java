package com.xj.payment_processor.service;

import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.model.User;
import com.xj.payment_processor.producer.TransactionEventProducer;
import com.xj.payment_processor.repository.TransactionRepository;
import com.xj.payment_processor.repository.UserRepository;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionEventProducer transactionEventProducer;
    private final RedisTemplate<String, String> redisTemplate;

    public TransactionService(TransactionRepository transactionRepository, 
                            UserRepository userRepository,
                            TransactionEventProducer transactionEventProducer,
                            RedisTemplate<String, String> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionEventProducer = transactionEventProducer;
        this.redisTemplate = redisTemplate;
    }

    public Transaction initiateTransfer(Long senderId, Long receiverId, Double amount) throws Exception {
        Optional<User> optSender = userRepository.findById(senderId);
        Optional<User> optReceiver = userRepository.findById(receiverId);

        if (optSender.isEmpty() || optReceiver.isEmpty()) {
            throw new Exception("Sender or receiver not found");
        }

        User sender = optSender.get();
        User receiver = optReceiver.get();

        // Create initial transaction record with PENDING status
        Transaction transaction = new Transaction(sender, receiver, amount);
        transaction.setStatus("PENDING");
        redisTemplate.opsForValue().set("txn_status:" + transaction.getId(), "PENDING", 5, TimeUnit.MINUTES);
        transactionRepository.save(transaction);

        // Send to Kafka for processing
        transactionEventProducer.sendTransactionEvent(
            transaction.getId(),
            senderId.toString(),
            receiverId.toString(),
            amount.toString()
        );
        
        return transaction;
    }

    public Transaction getTransaction(Long transactionId) throws Exception {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new Exception("Transaction not found"));
    }

    public String getTransactionStatus(Long transactionId) {
        String status = redisTemplate.opsForValue().get("txn_status:" + transactionId);
        if (status == null) {
            // Fallback to database if Redis doesn't have the status
            try {
                Transaction transaction = getTransaction(transactionId);
                return transaction.getStatus();
            } catch (Exception e) {
                return "UNKNOWN";
            }
        }
        return status;
    }
}
