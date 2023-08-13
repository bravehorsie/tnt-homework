package com.grigoriadi.tnt.homework.service;

import com.grigoriadi.tnt.homework.aggregation.track.TrackWorker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements TrackService {

    private final TrackWorker trackWorker;

    public TrackServiceImpl(TrackWorker trackWorker) {
        this.trackWorker = trackWorker;
    }

    @Override
    public CompletableFuture<Map<String, String>>  getTrack(List<String> shipmentIds) {
        Map<String, CompletableFuture<String>> trackFutures = new HashMap<>();
        shipmentIds.forEach(trackId -> trackFutures.put(trackId, trackWorker.submitTrack(trackId)));

        return CompletableFuture.allOf(trackFutures.values().toArray(new CompletableFuture[0]))
                .thenApply(unused -> {
                            Map<String, String> resultMap = new HashMap<>();
                            trackFutures.forEach((key, value) -> resultMap.put(key, value.join()));
                            return resultMap;
                        }
                );
    }
}
