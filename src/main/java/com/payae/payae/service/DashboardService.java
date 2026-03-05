package com.payae.payae.service;

import com.payae.payae.dto.DashboardResponse;
import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PaymentRepository;
import com.payae.payae.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    public DashboardResponse getDashboard(User user){

        Double totalInvested = paymentRepository.sumPaymentsByUser(user);

        Portfolio portfolio = portfolioRepository.findByUser(user);

        double savings = 0;
        double mf = 0;
        double gold = 0;

        if(portfolio != null){
            savings = portfolio.getSavingsBalance();
            mf = portfolio.getMutualFundUnits();
            gold = portfolio.getGoldGrams();
        }

        return new DashboardResponse(
                totalInvested,
                savings,
                mf,
                gold
        );
    }
}