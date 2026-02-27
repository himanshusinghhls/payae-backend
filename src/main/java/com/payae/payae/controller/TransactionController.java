package com.payae.payae.controller;

import com.payae.payae.entity.Transaction;
import com.payae.payae.entity.User;
import com.payae.payae.repository.TransactionRepository;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Transaction> getTransactions(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        return transactionRepository.findAll()
                .stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .toList();
    }
}