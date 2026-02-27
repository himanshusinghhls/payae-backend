package com.payae.payae.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioResponse {

    private double savingsBalance;
    private double mfUnits;
    private double goldGrams;
}