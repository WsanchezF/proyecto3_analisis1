package com.example.rupbank.repository;

import com.example.rupbank.model.Bank;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
}
