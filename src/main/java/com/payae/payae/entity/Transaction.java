package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double originalAmount;
    private Double roundUpAmount;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
}