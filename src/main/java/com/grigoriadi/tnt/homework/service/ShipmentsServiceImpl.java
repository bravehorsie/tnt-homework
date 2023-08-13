package com.grigoriadi.tnt.homework.service;

import com.grigoriadi.tnt.homework.aggregation.shipments.ShipmentsWorker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ShipmentsServiceImpl implements ShipmentsService {

    private final ShipmentsWorker shipmentsWorker;

    public ShipmentsServiceImpl(ShipmentsWorker shipmentsWorker) {
        this.shipmentsWorker = shipmentsWorker;
    }

    @Override
    public CompletableFuture<Map<String, List<String>>> getShipments(List<String> shipmentIds) {
        Map<String, CompletableFuture<List<String>>> shipmentFutures = new HashMap<>();
        shipmentIds.forEach(shipmentId->shipmentFutures.put(shipmentId, shipmentsWorker.submitShipments(shipmentId)));

        return CompletableFuture.allOf(shipmentFutures.values().toArray(new CompletableFuture[0]))
                .thenApply(unused -> {
                            Map<String, List<String>> resultMap = new HashMap<>();
                            shipmentFutures.forEach((key, value) -> resultMap.put(key, value.join()));
                            return resultMap;
                        }
                );
    }
}
