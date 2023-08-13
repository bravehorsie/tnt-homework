package com.grigoriadi.tnt.homework.aggregation.track;

import com.grigoriadi.tnt.homework.aggregation.AggregationWorkerImpl;
import com.grigoriadi.tnt.homework.aggregation.pricing.PricingWorker;
import com.grigoriadi.tnt.homework.client.PricingClient;
import com.grigoriadi.tnt.homework.client.TrackClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
public class TrackWorkerImpl extends AggregationWorkerImpl<String> implements TrackWorker {

    public TrackWorkerImpl(TrackClient trackClient) {
        super(trackClient::getTrack);
    }

    @Override
    public CompletableFuture<String> submitTrack(String trackid) {
        return submitForAggregation(trackid);
    }
}
