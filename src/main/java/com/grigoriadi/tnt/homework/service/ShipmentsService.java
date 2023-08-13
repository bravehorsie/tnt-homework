package com.grigoriadi.tnt.homework.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ShipmentsService {

    CompletableFuture<Map<String, List<String>>> getShipments(List<String> shipmentIds);
}
