package com.grigoriadi.tnt.homework.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PricingClient {
    Map<String, BigDecimal> getPricing(List<String> countries);
}
