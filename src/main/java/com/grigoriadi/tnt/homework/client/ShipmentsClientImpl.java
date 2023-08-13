package com.grigoriadi.tnt.homework.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ShipmentsClientImpl implements ShipmentsClient {

    private static final Logger log = LoggerFactory.getLogger(ShipmentsClientImpl.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;

    public ShipmentsClientImpl(RestTemplate restTemplate, @Value("${api.host.baseurl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Map<String, List<String>> getShipments(List<String> shipments) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/shipments")
                .queryParam("q", shipments)
                .build();
        ParameterizedTypeReference<Map<String, List<String>>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, List<String>>> response = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, typeReference);

        log.info("Got shipments: {}", response.getBody());
        return response.getBody();
    }


}
