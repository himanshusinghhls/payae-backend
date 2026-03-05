package com.payae.payae.entity;
import jakarta.persistence.*;

@Entity
public class RoundUpSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private Double fixedValue;

    private Double percentage;

    private Boolean paused;

    private Double monthlyCap;

    private Double monthlySaved;

    @OneToOne
    private User user;

    public RoundUpSettings() {}

    // getters setters
}