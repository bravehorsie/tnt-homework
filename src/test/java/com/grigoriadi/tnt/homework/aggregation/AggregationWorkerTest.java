package com.grigoriadi.tnt.homework.aggregation;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AggregationWorkerTest {

    @Test
    public void testSingleCallAfterOnConsumeTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        Integer expectedCount = 1;
        CountClientCallsFunction countFunc = new CountClientCallsFunction(expectedCount);
        AggregationWorker<String> aggregationWorker = getTestAggregationWorker(countFunc);
        CompletableFuture<String> abc = aggregationWorker.submitForAggregation("ABC");
        assertTrue(countFunc.awaitAllAPICalls());
        assertEquals("ABC-value", abc.get(1, TimeUnit.MILLISECONDS));
        assertEquals(List.of("ABC"), countFunc.getAllPassedKeys());
    }

    @Test
    public void testSingleCallOnSufficientKeyCount() throws ExecutionException, InterruptedException, TimeoutException {
        Integer expectedCount = 1;
        CountClientCallsFunction countFunc = new CountClientCallsFunction(expectedCount);
        AggregationWorker<String> aggregationWorker = getTestAggregationWorker(countFunc);
        CompletableFuture<String> abc = aggregationWorker.submitForAggregation("ABC");
        CompletableFuture<String> def = aggregationWorker.submitForAggregation("DEF");
        CompletableFuture<String> ghi = aggregationWorker.submitForAggregation("GHI");
        assertTrue(countFunc.awaitAllAPICalls());
        assertEquals("ABC-value", abc.get(1, TimeUnit.MILLISECONDS));
        assertEquals("DEF-value", def.get(1, TimeUnit.MILLISECONDS));
        assertEquals("GHI-value", ghi.get(1, TimeUnit.MILLISECONDS));
        assertEquals(List.of("ABC", "DEF", "GHI"), countFunc.getAllPassedKeys());
    }

    @Test
    public void test729Calls() throws ExecutionException, InterruptedException, TimeoutException {
        Integer expectedCount = 729;
        CountClientCallsFunction countFunc = new CountClientCallsFunction(expectedCount);
        AggregationWorker<String> aggregationWorker = getTestAggregationWorker(countFunc);
        List<CompletableFuture<String>> allFutures = new ArrayList<>();
        IntStream.range(0, expectedCount).forEach(i -> {
            //batch size for client call is set to 3 in this test, so submitting 3 keys would result to
            allFutures.add(aggregationWorker.submitForAggregation(getRandomThreeCharString()));
            allFutures.add(aggregationWorker.submitForAggregation(getRandomThreeCharString()));
            allFutures.add(aggregationWorker.submitForAggregation(getRandomThreeCharString()));
        });
        CompletableFuture<Void> unionFuture = CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
        assertTrue(countFunc.awaitAllAPICalls());
        unionFuture.get(3, TimeUnit.SECONDS);
        assertEquals(expectedCount * 3, countFunc.getAllPassedKeys().size());
    }

    private AggregationWorker<String> getTestAggregationWorker(CountClientCallsFunction countClientCallsFunction) {
        AggregationWorkerImpl<String> aggregationWorker = new AggregationWorkerImpl<>(countClientCallsFunction);
        aggregationWorker.setTimeoutSeconds(2);
        aggregationWorker.setBatchSize(3);
        aggregationWorker.afterPropertiesSet();
        return aggregationWorker;
    }

    private String getRandomThreeCharString() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder result = new StringBuilder();
        Random rnd = new SecureRandom();
        while (result.length() < 3) {
            int index = rnd.nextInt(0, CHARS.length());
            result.append(CHARS.charAt(index));
        }
        return result.toString();
    }

    /**
     * Instead of mocking REST client call with Mockito, just pass a substitute function mocking
     * result and counting each client call.
     */
    private static final class CountClientCallsFunction implements Function<List<String>, Map<String, String>> {
        private final CountDownLatch clientCallsLatch;
        private final List<String> allPassedKeys = Collections.synchronizedList(new ArrayList<>());

        private CountClientCallsFunction(Integer expectedClientCallsCount) {
            Objects.requireNonNull(expectedClientCallsCount);
            clientCallsLatch = new CountDownLatch(expectedClientCallsCount);
        }

        @Override
        public Map<String, String> apply(List<String> strings) {
            clientCallsLatch.countDown();
            allPassedKeys.addAll(strings);
            return strings.stream().collect(Collectors.toMap(s -> s, s -> s + "-value"));
        }

        public boolean awaitAllAPICalls() {
            try {
                return clientCallsLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        public List<String> getAllPassedKeys() {
            return allPassedKeys;
        }
    }
}
