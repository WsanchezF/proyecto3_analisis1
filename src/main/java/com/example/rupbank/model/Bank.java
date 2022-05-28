package com.example.rupbank.model;

import javax.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "bank")
@EntityListeners(AuditingEntityListener.class)
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo")
    private String logo;

    @Column(name = "last_number_account_issued")
    private Integer lastNumberAccountIssued = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getLastNumberAccountIssued() {
        return lastNumberAccountIssued;
    }

    public void setLastNumberAccountIssued(Integer lastNumberAccountIssued) {
        this.lastNumberAccountIssued = lastNumberAccountIssued;
    }
}