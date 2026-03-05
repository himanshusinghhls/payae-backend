package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    @Column(nullable = false, unique = true)
    private String razorpayPaymentId;   
    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
}