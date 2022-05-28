package com.example.rupbank.repository;

import com.example.rupbank.model.CreditCard;
import com.example.rupbank.model.CreditCardTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransaction, Long> {
    @Query("SELECT t FROM CreditCardTransaction t WHERE t.creditCard = :creditCard")
    public List<CreditCardTransaction> findByCard(@Param("creditCard") CreditCard card);
}
