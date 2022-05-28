package com.example.rupbank.repository;

import com.example.rupbank.model.Service;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
}
