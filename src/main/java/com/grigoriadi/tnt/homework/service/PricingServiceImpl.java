package com.grigoriadi.tnt.homework.service;

import com.grigoriadi.tnt.homework.aggregation.pricing.PricingWorker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingWorker pricingWorker;

    public PricingServiceImpl(PricingWorker pricingWorker) {
        this.pricingWorker = pricingWorker;
    }

    @Override
    public CompletableFuture<Map<String, BigDecimal>> getPricing(List<String> countries) {
        Map<String, CompletableFuture<BigDecimal>> pricingFutures = new HashMap<>();
        countries.forEach(country->pricingFutures.put(country, pricingWorker.submitPricing(country)));

        return CompletableFuture.allOf(pricingFutures.values().toArray(new CompletableFuture[0]))
                .thenApply(unused -> {
                            Map<String, BigDecimal> resultMap = new HashMap<>();
                            pricingFutures.forEach((key, value) -> resultMap.put(key, value.join()));
                            return resultMap;
                        }
                );
    }
}
