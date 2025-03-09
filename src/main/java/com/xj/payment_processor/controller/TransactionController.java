package com.xj.payment_processor.controller;

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
            Transaction transaction = transactionService.transferMoney(senderId, receiverId, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
