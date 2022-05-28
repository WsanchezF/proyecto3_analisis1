package com.example.rupbank.repository;

import com.example.rupbank.model.Account;
import com.example.rupbank.model.Bank;
import com.example.rupbank.model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT a FROM Account a WHERE a.customer = :customer AND a.bank = :bank")
    public List<Account> findByCustomerAndBank(@Param("customer") Customer customer, @Param("bank") Bank bank);

    @Query("SELECT a FROM Account a WHERE a.bank = :bank")
    public List<Account> findByBank(@Param("bank") Bank bank);
}