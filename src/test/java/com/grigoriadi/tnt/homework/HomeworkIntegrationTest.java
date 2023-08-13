package com.grigoriadi.tnt.homework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "integrationTest", matches = "true")
public class HomeworkIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(HomeworkIntegrationTest.class);

    //Maximum 10 requests async
    private final ExecutorService mockMvcExecutor = Executors.newFixedThreadPool(10);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @IfProfileValue(name="integration")
    public void testAsyncRequests() throws Exception {
        //execute some requests and assert responses are 200ok and contains requested keys
        //since there is contract to return null on backing service errors, not much left to assume here
        List<CompletableFuture<ResultActions>> mockMvcCallFutures = new ArrayList<>();
        String[] isoCountries = Locale.getISOCountries();
        Random random = new SecureRandom();
        IntStream.range(0, 50).forEach(i -> {
            List<String> countries = new ArrayList<>();
            List<String> track = new ArrayList<>();
            List<String> shipments = new ArrayList<>();
            CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(() -> {
                        //fire 2-5 keys each category in single request
                        IntStream.range(0, random.nextInt(2,6)).forEach(nextIndex -> {
                            countries.add(isoCountries[random.nextInt(0, isoCountries.length)]);
                            track.add(String.valueOf(Math.abs(random.nextInt())));
                            shipments.add(String.valueOf(Math.abs(random.nextInt())));
                        });
                        return performAggregationRequest(countries, track, shipments);
                    }, mockMvcExecutor)
                    .whenComplete((resultActions, throwable) -> {
                        if (throwable != null) {
                            throw new RuntimeException(throwable);
                        }
                        verifyRequestKyesPresentInResponse(countries, track, shipments, resultActions);
                    });
            mockMvcCallFutures.add(future);

        });
        CompletableFuture.allOf(mockMvcCallFutures.toArray(new CompletableFuture[0])).join();
    }

    private void verifyRequestKyesPresentInResponse(List<String> countries, List<String> track, List<String> shipments, ResultActions resultActions) {
        Map<String, Object> resultMap;
        try {
            ResultActions actions = resultActions.andExpect(status().isOk());
            String responseBody = actions.andReturn().getResponse().getContentAsString();
            log.info("MockMvc request completed: {}", responseBody);
            resultMap = objectMapper.readValue(responseBody.getBytes(StandardCharsets.UTF_8), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(resultMap.get("pricing"));
        assertNotNull(resultMap.get("track"));
        assertNotNull(resultMap.get("shipments"));
        assertTrue(resultMap.get("pricing") instanceof Map);
        assertTrue(resultMap.get("track") instanceof Map);
        assertTrue(resultMap.get("shipments") instanceof Map);
        countries.forEach(countryKey-> assertTrue(((Map) resultMap.get("pricing")).containsKey(countryKey)));
        track.forEach(trackKey-> assertTrue(((Map) resultMap.get("track")).containsKey(trackKey)));
        shipments.forEach(countryKey-> assertTrue(((Map) resultMap.get("shipments")).containsKey(countryKey)));
    }

    private ResultActions performAggregationRequest(List<String> pricing, List<String> track, List<String> shipments) {
        try {
            return mockMvc.perform(get("/aggregation")
                    .contentType("application/json")
                    .queryParam("pricing", pricing.toArray(new String[0]))
                    .queryParam("track", track.toArray(new String[0]))
                    .queryParam("shipments", shipments.toArray(new String[0]))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
