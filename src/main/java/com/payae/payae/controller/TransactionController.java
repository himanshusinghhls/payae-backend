package com.payae.payae.controller;

import com.payae.payae.entity.Ledger;
import com.payae.payae.entity.User;
import com.payae.payae.repository.LedgerRepository;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Ledger> getTransactions(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        return ledgerRepository.findAll()
                .stream()
                .filter(l -> l.getUser().getId().equals(user.getId()))
                .toList();
    }
}