package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Double savingsBalance = 0.0;

    @Builder.Default
    private Double mfUnits = 0.0;

    @Builder.Default
    private Double goldGrams = 0.0;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}