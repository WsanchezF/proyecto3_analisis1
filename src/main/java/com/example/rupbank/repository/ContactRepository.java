package com.example.rupbank.repository;

import com.example.rupbank.model.Contact;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
