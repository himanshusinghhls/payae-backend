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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private Double bankBalance = 10000.0;

    private String roundupType;
    private Double roundupValue;

    private Double allocationSavings;
    private Double allocationMf;
    private Double allocationGold;

    private boolean autoSavingPaused;

    private Double monthlyCap;
    
    @Builder.Default
    private String pin = "0000";
    
    @Builder.Default
    private boolean hasCompletedOnboarding = false;

    private LocalDateTime createdAt;
}