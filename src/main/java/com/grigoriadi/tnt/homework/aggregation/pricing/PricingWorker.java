package com.grigoriadi.tnt.homework.aggregation.pricing;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface PricingWorker {
    CompletableFuture<BigDecimal> submitPricing(String country);
}