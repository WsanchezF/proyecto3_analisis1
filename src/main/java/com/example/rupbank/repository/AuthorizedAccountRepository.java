package com.example.rupbank.repository;

import com.example.rupbank.model.AuthorizedAccount;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AuthorizedAccountRepository extends JpaRepository<AuthorizedAccount, Long> {
}
