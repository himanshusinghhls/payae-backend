package com.payae.payae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.payae.payae.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query(value = "SELECT * FROM users WHERE LOWER(REGEXP_REPLACE(SPLIT_PART(email, '@', 1), '[^a-z0-9]', '', 'g')) || '@payae' = LOWER(:payaeId)", nativeQuery = true)
    Optional<User> findByPayaeId(@Param("payaeId") String payaeId);
}