package com.xj.payment_processor.controller;

import java.util.Map;

import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam Double amount) {
            try {
                // Publish event to Kafka
                Transaction pendingTransaction = transactionService.initiateTransfer(senderId, receiverId, amount);
                Long transactionId = pendingTransaction.getId();
                
                // Poll Redis for the transaction status
                String transactionStatus;
                int retries = 10;
        
                do {
                    transactionStatus = transactionService.getTransactionStatus(transactionId);
                    if ("COMPLETED".equals(transactionStatus)) {
                        return ResponseEntity.ok(Map.of(
                            "message", "TRANSFER SUCCESS",
                            "transactionId", pendingTransaction.getId(),
                            "status", transactionStatus
                        ));
                    }
                    if ("FAILED".equals(transactionStatus)) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "message", "TRANSFER FAIL",
                            "transactionId", pendingTransaction.getId(),
                            "status", transactionStatus
                        ));
                    }
        
                    Thread.sleep(500);
                    retries--;
                } while (retries > 0);
        
                // timeout response
                return ResponseEntity.accepted().body(Map.of(
                    "message", "TRANSFER PROCESSING",
                    "transactionId", pendingTransaction.getId(),
                    "status", "PENDING"
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
    }
}
