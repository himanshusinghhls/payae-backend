package com.payae.payae.mapper;

import com.payae.payae.dto.PortfolioResponse;
import com.payae.payae.entity.Portfolio;

public class PortfolioMapper {

    public static PortfolioResponse toResponse(Portfolio portfolio){

        return new PortfolioResponse(
                portfolio.getSavingsBalance(),
                portfolio.getMfUnits(),
                portfolio.getGoldGrams()
        );
    }
}