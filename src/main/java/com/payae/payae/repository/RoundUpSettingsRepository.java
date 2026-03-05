package com.payae.payae.repository;

import com.payae.payae.entity.RoundUpSettings;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundUpSettingsRepository extends JpaRepository<RoundUpSettings, Long> {

    Optional<RoundUpSettings> findByUser(User user);
}