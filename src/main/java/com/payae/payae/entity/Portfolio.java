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

    private Double savingsBalance;
    private Double mfUnits;
    private Double goldGrams;

    @OneToOne
    private User user;
}