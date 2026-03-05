package com.payae.payae.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allocation_settings")
public class AllocationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double savingsPercent;
    private Double mutualFundPercent;
    private Double goldPercent;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public AllocationSettings() {}

    public Long getId() {
        return id;
    }

    public Double getSavingsPercent() {
        return savingsPercent;
    }

    public void setSavingsPercent(Double savingsPercent) {
        this.savingsPercent = savingsPercent;
    }

    public Double getMutualFundPercent() {
        return mutualFundPercent;
    }

    public void setMutualFundPercent(Double mutualFundPercent) {
        this.mutualFundPercent = mutualFundPercent;
    }

    public Double getGoldPercent() {
        return goldPercent;
    }

    public void setGoldPercent(Double goldPercent) {
        this.goldPercent = goldPercent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}