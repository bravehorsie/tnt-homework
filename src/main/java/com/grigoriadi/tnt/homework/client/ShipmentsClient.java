package com.grigoriadi.tnt.homework.client;

import java.util.List;
import java.util.Map;

public interface ShipmentsClient {
    Map<String, List<String>> getShipments(List<String> shipmentIds);
}
