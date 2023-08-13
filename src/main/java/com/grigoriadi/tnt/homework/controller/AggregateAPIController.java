package com.grigoriadi.tnt.homework.controller;

import com.grigoriadi.tnt.homework.service.PricingService;
import com.grigoriadi.tnt.homework.service.ShipmentsService;
import com.grigoriadi.tnt.homework.service.TrackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
public class AggregateAPIController {

    private final PricingService pricingService;
    private final ShipmentsService shipmentsService;
    private final TrackService trackService;

    private Integer aggregateControllerTimeout;

    public AggregateAPIController(PricingService pricingService, ShipmentsService shipmentsService, TrackService trackService) {
        this.pricingService = pricingService;
        this.shipmentsService = shipmentsService;
        this.trackService = trackService;
    }

    private static final Logger log = LoggerFactory.getLogger(AggregateAPIController.class);
    @GetMapping("/aggregation")
    public ResponseEntity<Map<String, Object>> getAggregations(
            @RequestParam Optional<List<String>> pricing,
            @RequestParam Optional<List<String>> track,
            @RequestParam Optional<List<String>> shipments) {

        //collect futures for the results
        CompletableFuture<Map<String, BigDecimal>> pricingFuture = pricing.isPresent() ?
                pricingService.getPricing(pricing.get()) : CompletableFuture.completedFuture(new HashMap<>());

        CompletableFuture<Map<String, String>> trackFuture = track.isPresent() ?
                trackService.getTrack(track.get()) : CompletableFuture.completedFuture(new HashMap<>());

        CompletableFuture<Map<String, List<String>>> shipmentFuture = shipments.isPresent() ?
                shipmentsService.getShipments(shipments.get()) : CompletableFuture.completedFuture(new HashMap<>());

        try {
            //wait for the timeout specified
            CompletableFuture<Void> unionFuture = CompletableFuture.allOf(pricingFuture, trackFuture, shipmentFuture);
            unionFuture.get(aggregateControllerTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted waiting: {}", e.getMessage());
        } catch (ExecutionException e) {
            log.warn("Error invoking API:", e);
        } catch (TimeoutException e) {
            log.warn("Timeout after waiting for {} seconds", aggregateControllerTimeout);
        }

        //collect results
        Map<String, Object> response = new HashMap<>();
        putPricingResults(pricingFuture, pricing, response, "pricing");
        putPricingResults(trackFuture, track, response, "track");
        putPricingResults(shipmentFuture, shipments, response, "shipments");

        log.info("Aggregated API response: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private <T> void putPricingResults(CompletableFuture<Map<String, T>> resultsFuture, Optional<List<String>> requestParams, Map<String, Object> response, String key) {
        if (requestParams.isEmpty()) {
            return;
        }
        //just check for timeout here, no need for future.isCompletedExceptionally, as worker checks client errors and set nulls beforehand
        if (resultsFuture.isDone()) {
            response.put(key, resultsFuture.join());
        } else {
            Map<String, T> resultNullValueMap = new HashMap<>();
            requestParams.get().forEach(s->resultNullValueMap.put(s, null));
            response.put(key, resultNullValueMap);
        }
    }

    @Value("${aggregate.timeout.seconds}")
    public void setAggregateControllerTimeout(Integer aggregateControllerTimeout) {
        this.aggregateControllerTimeout = aggregateControllerTimeout;
    }
}
