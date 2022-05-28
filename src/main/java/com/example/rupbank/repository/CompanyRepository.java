package com.example.rupbank.repository;

import com.example.rupbank.model.Company;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
