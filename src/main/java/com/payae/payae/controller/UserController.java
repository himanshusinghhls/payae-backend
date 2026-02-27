package com.payae.payae.controller;

import com.payae.payae.entity.User;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/toggle-pause")
    public void togglePause(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        user.setAutoSavingPaused(!user.isAutoSavingPaused());

        userRepository.save(user);
    }
}