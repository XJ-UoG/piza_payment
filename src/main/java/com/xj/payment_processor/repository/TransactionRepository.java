package com.xj.payment_processor.repository;

import com.xj.payment_processor.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t.status FROM Transaction t WHERE t.id = :transactionId")
    String findStatusByTransactionId(@Param("transactionId") Long transactionId);
}
