package com.payae.payae.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String roundupType; // FIXED or PERCENT
    private Double roundupValue;

    private Double allocationSavings;
    private Double allocationMf;
    private Double allocationGold;

    private boolean autoSavingPaused;

    private Double monthlyCap;

    private LocalDateTime createdAt;
}