package com.grigoriadi.tnt.homework.aggregation.shipments;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ShipmentsWorker {
    CompletableFuture<List<String>> submitShipments(String shipmentId);
}