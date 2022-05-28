package com.example.rupbank.repository;

import com.example.rupbank.model.Bank;
import com.example.rupbank.model.CreditCard;
import com.example.rupbank.model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    @Query("SELECT c FROM CreditCard c WHERE c.customer = :customer AND c.bank = :bank")
    public List<CreditCard> findByCustomerAndBank(@Param("customer") Customer customer, @Param("bank") Bank bank);
}
