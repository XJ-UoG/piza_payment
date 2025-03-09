package com.xj.payment_processor.service;

import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.model.User;
import com.xj.payment_processor.producer.TransactionEventProducer;
import com.xj.payment_processor.repository.TransactionRepository;
import com.xj.payment_processor.repository.UserRepository;

import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionEventProducer transactionEventProducer;

    public TransactionService(TransactionRepository transactionRepository, 
                            UserRepository userRepository,
                            TransactionEventProducer transactionEventProducer) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionEventProducer = transactionEventProducer;
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
}
