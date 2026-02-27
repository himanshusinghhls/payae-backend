package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // PAYMENT, ROUNDUP, ALLOCATION
    private String assetType; // SAVINGS, MF, GOLD

    private Double amount;

    private String paymentRef;

    private LocalDateTime timestamp;

    @ManyToOne
    private User user;
}