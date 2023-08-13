package com.grigoriadi.tnt.homework.aggregation;

import java.util.concurrent.CompletableFuture;

/**
 * Buffers key items for particular API in a queue, with a dedicated worker to read and send batch requests on API.
 * @param <T> API result type
 */
public interface AggregationWorker<T> {

    /**
     * Submit item on the queue.
     * @param key item to submit
     * @return CompletableFuture with later manual completion by worker.
     */
    CompletableFuture<T> submitForAggregation(String key);
}
