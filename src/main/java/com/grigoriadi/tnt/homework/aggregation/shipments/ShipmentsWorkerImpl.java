package com.grigoriadi.tnt.homework.aggregation.shipments;

import com.grigoriadi.tnt.homework.aggregation.AggregationWorkerImpl;
import com.grigoriadi.tnt.homework.client.PricingClient;
import com.grigoriadi.tnt.homework.client.ShipmentsClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShipmentsWorkerImpl extends AggregationWorkerImpl<List<String>> implements ShipmentsWorker {

    public ShipmentsWorkerImpl(ShipmentsClient shipmentsClient) {
        super(shipmentsClient::getShipments);
    }

    @Override
    public CompletableFuture<List<String>> submitShipments(String country) {
        return submitForAggregation(country);
    }
}
