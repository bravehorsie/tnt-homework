package com.grigoriadi.tnt.homework.aggregation.pricing;

import com.grigoriadi.tnt.homework.aggregation.AggregationWorkerImpl;
import com.grigoriadi.tnt.homework.client.PricingClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
public class PricingWorkerImpl extends AggregationWorkerImpl<BigDecimal> implements PricingWorker {

    public PricingWorkerImpl(PricingClient pricingClient) {
        super(pricingClient::getPricing);
    }

    @Override
    public CompletableFuture<BigDecimal> submitPricing(String country) {
        return submitForAggregation(country);
    }
}
