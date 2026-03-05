package com.payae.payae.repository;

import com.payae.payae.entity.AllocationSettings;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllocationSettingsRepository extends JpaRepository<AllocationSettings, Long> {

    Optional<AllocationSettings> findByUser(User user);

}