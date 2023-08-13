package com.grigoriadi.tnt.homework.aggregation;

import com.grigoriadi.tnt.homework.aggregation.model.PerKeyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AggregationWorkerImpl<T> implements AggregationWorker<T>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AggregationWorkerImpl.class);
    private final LinkedBlockingQueue<PerKeyResult<T>> keyQueue = new LinkedBlockingQueue<>();
    private final ExecutorService queueConsumer = Executors.newSingleThreadExecutor();
    private final ExecutorService clientApiCalls = Executors.newFixedThreadPool(10);
    private final Function<List<String>, Map<String, T>> clientCall;

    private Integer batchSize;
    private Integer timeoutSeconds;
    private Duration sendTimeout;

    public AggregationWorkerImpl(Function<List<String>, Map<String, T>> clientCallFunction) {
        this.clientCall = clientCallFunction;
    }

    @Override
    public CompletableFuture<T> submitForAggregation(String key) {
        PerKeyResult<T> perKeyResult = new PerKeyResult<>(key);
        try {
            keyQueue.put(perKeyResult);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        }
        return perKeyResult.getResultFuture();
    }

    @Value("${worker.batchSize}")
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Value("${worker.timeoutSeconds}")
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void afterPropertiesSet() {
        this.sendTimeout = Duration.of(timeoutSeconds, ChronoUnit.SECONDS);
        queueConsumer.submit(new ClientRequestAggregationRunnable(clientCall));
    }

    private final class ClientRequestAggregationRunnable implements Runnable {

        private final Function<List<String>, Map<String, T>> clientCallFunction;

        private ClientRequestAggregationRunnable(Function<List<String>, Map<String, T>> clientCallFunction) {
            this.clientCallFunction = clientCallFunction;
        }

        @Override
        public void run() {
            //keep order, remove duplicates
            List<PerKeyResult<T>> resultFuturesBatch = new ArrayList<>(batchSize);
            Instant lastPoll = Instant.now();
            while (!queueConsumer.isShutdown()) {
                PerKeyResult<T> nextKeyResult = null;
                try {
                    nextKeyResult = keyQueue.poll(timeoutSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.info("Interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
                if (nextKeyResult != null) {
                    resultFuturesBatch.add(nextKeyResult);
                    lastPoll = Instant.now();
                }
                if (resultFuturesBatch.size() == batchSize || (resultFuturesBatch.size() > 0 && Duration.between(lastPoll, Instant.now()).compareTo(sendTimeout) > 0)) {
                    List<PerKeyResult<T>> resultFuturesCopy = new ArrayList<>(resultFuturesBatch);
                    resultFuturesBatch.clear();
                    callClient(resultFuturesCopy);
                }
            }
        }

        private void callClient(List<PerKeyResult<T>> resultFutures) {
            CompletableFuture<Map<String, T>> clientResult = CompletableFuture.supplyAsync(() -> clientCallFunction.apply(resultFutures.stream().map(PerKeyResult::getKey).toList()), clientApiCalls);
            clientResult.whenComplete((results, throwable) -> {
                if (throwable != null) {
                    log.warn("REST client exception", throwable);
                    //per task description set nulls on error
                    resultFutures.forEach(result -> result.getResultFuture().complete(null));
                    return;
                }
                //complete futures with client result
                resultFutures.forEach(result->result.getResultFuture().complete(results.get(result.getKey())));
            });
        }
    }

}
