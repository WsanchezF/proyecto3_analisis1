package com.example.rupbank.repository;

import com.example.rupbank.model.Account;
import com.example.rupbank.model.Customer;
import com.example.rupbank.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.account = :account")
    public List<Transaction> findByAccount(@Param("account") Account account);
}
