package com.grigoriadi.tnt.homework.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TrackService {

    CompletableFuture<Map<String, String>> getTrack(List<String> trackId);
}
