package com.xj.payment_processor.service;

import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.TransactionRepository;
import com.xj.payment_processor.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
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

        Transaction transaction = new Transaction(sender, receiver, amount);
        return transactionRepository.save(transaction);
    }
}
