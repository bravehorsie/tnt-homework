package com.grigoriadi.tnt.homework.aggregation.track;

import java.util.concurrent.CompletableFuture;

public interface TrackWorker {
    CompletableFuture<String> submitTrack(String trackId);
}