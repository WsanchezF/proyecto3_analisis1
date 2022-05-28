package com.example.rupbank.repository;

import com.example.rupbank.model.Bank;
import com.example.rupbank.model.Customer;
import com.example.rupbank.model.Loan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("SELECT l FROM Loan l WHERE l.customer = :customer AND l.bank = :bank")
    public List<Loan> findByCustomerAndBank(@Param("customer") Customer customer, @Param("bank") Bank bank);
}
