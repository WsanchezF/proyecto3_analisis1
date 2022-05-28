package com.example.rupbank.repository;

import com.example.rupbank.model.Check;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CheckRepository extends JpaRepository<Check, Long> {
}
