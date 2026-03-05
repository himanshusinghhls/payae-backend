package com.payae.payae.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MarketSimulationService {

    private double nav = 100;

    private double goldPrice = 6000;

    private Random random = new Random();

    public double getNav(){

        double change = random.nextDouble() * 2 - 1;

        nav = nav + change;

        if(nav < 50){
            nav = 50;
        }

        return nav;

    }

    public double getGoldPrice(){

        double change = random.nextDouble() * 20 - 10;

        goldPrice = goldPrice + change;

        if(goldPrice < 4000){
            goldPrice = 4000;
        }

        return goldPrice;

    }

}