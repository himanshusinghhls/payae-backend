package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double savingsBalance = 0.0;

    @Column(nullable = false)
    private Double mfUnits = 0.0;

    @Column(nullable = false)
    private Double goldGrams = 0.0;
    @OneToOne
    private User user;
}