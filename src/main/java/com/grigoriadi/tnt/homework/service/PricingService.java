package com.grigoriadi.tnt.homework.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PricingService {

    CompletableFuture<Map<String, BigDecimal>> getPricing(List<String> country);
}
