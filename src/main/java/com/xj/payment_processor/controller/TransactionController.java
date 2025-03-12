package com.xj.payment_processor.controller;

import com.xj.payment_processor.config.ResponseStorage;
import com.xj.payment_processor.model.Transaction;
import com.xj.payment_processor.service.TransactionService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final ResponseStorage ResponseStorage;

    public TransactionController(TransactionService transactionService, ResponseStorage ResponseStorage) {
        this.ResponseStorage = ResponseStorage;
        this.transactionService = transactionService;
    }
    
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam Double amount) {
        try {
            // Publish event to Kafka
            Transaction transaction = transactionService.initiateTransfer(senderId, receiverId, amount);

            // Wait for response, transactionID will be used as requestID for response
            Long transactionId = transaction.getId();
            String requestId = String.valueOf(transactionId);

            CompletableFuture<String> future = ResponseStorage.waitForResponse(requestId);
            String status = future.get(3, TimeUnit.SECONDS);
            return ResponseEntity.ok(Map.of(
                "message", "Transaction Successful",
                "transactionId", requestId,
                "status", status
            ));
        } catch (TimeoutException e) {
            return ResponseEntity.accepted().body(Map.of(
                "message", "Transaction is processing",
                "status", "PENDING"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
