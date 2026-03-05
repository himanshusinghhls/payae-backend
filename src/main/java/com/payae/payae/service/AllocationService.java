package com.payae.payae.service;

import com.payae.payae.entity.AllocationSettings;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AllocationService {

    public Map<String, Double> allocate(double amount, AllocationSettings settings) {

        Map<String, Double> allocationMap = new HashMap<>();

        double savings = amount * settings.getSavingsPercent() / 100;
        double mutualFund = amount * settings.getMutualFundPercent() / 100;
        double gold = amount * settings.getGoldPercent() / 100;

        allocationMap.put("SAVINGS", savings);
        allocationMap.put("MUTUAL_FUND", mutualFund);
        allocationMap.put("GOLD", gold);

        return allocationMap;
    }
}