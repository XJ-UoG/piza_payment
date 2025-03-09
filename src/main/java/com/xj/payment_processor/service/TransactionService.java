package com.xj.payment_processor.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.TransactionRepository;
import com.xj.payment_processor.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Transaction transferMoney(Long senderId, Long receiverId, Double amount) throws Exception {
        Optional<User> optSender = userRepository.findById(senderId);
        Optional<User> optReceiver = userRepository.findById(receiverId);

        if (optSender.isEmpty() || optReceiver.isEmpty()) {
            throw new Exception("Sender or receiver not found");
        }

        User sender = optSender.get();
        User receiver = optReceiver.get();

        if (sender.getBalance() < amount) {
            throw new Exception("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - amount);
        userRepository.save(sender);
        receiver.setBalance(receiver.getBalance() + amount);
        userRepository.save(receiver);

        String senderKey = "user_balance:" + senderId;
        String receiverKey = "user_balance:" + receiverId;
        
        // redisTemplate.opsForValue().set(senderKey, String.valueOf(sender.getBalance()), 5, TimeUnit.MINUTES);
        // redisTemplate.opsForValue().set(receiverKey, String.valueOf(receiver.getBalance()), 5, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(senderKey, String.valueOf(sender.getUsername()), 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(receiverKey, String.valueOf(receiver.getUsername()), 5, TimeUnit.MINUTES);

        Transaction transaction = new Transaction(sender, receiver, amount);
        return transactionRepository.save(transaction);
    }
}
