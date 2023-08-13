package com.grigoriadi.tnt.homework.aggregation.model;

import java.util.concurrent.CompletableFuture;

public class PerKeyResult<T> {
    private final String key;
    private final CompletableFuture<T> resultFuture;

    public PerKeyResult(String key) {
        this.key = key;
        resultFuture = new CompletableFuture<>();
    }

    public String getKey() {
        return key;
    }

    public CompletableFuture<T> getResultFuture() {
        return resultFuture;
    }
}
