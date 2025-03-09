package com.xj.payment_processor.repository;

import com.xj.payment_processor.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    
}
