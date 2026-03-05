package com.payae.payae.controller;

import com.payae.payae.dto.PortfolioResponse;
import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.mapper.PortfolioMapper;
import com.payae.payae.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @GetMapping
    public PortfolioResponse getPortfolio(@RequestAttribute User user){

        Portfolio portfolio = portfolioRepository.findByUser(user);

        if(portfolio == null){
            return new PortfolioResponse(0.0,0.0,0.0);
        }

        return PortfolioMapper.toResponse(portfolio);
    }
}