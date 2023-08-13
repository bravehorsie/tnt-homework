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

import java.util.List;
import java.util.Map;

@Component
public class TrackClientImpl implements TrackClient {

    private static final Logger log = LoggerFactory.getLogger(TrackClientImpl.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;

    public TrackClientImpl(RestTemplate restTemplate, @Value("${api.host.baseurl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getTrack(List<String> trackIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/track")
                .queryParam("q", trackIds)
                .build();
        ParameterizedTypeReference<Map<String, String>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, typeReference);

        log.info("Got track: {}", response.getBody());
        return response.getBody();
    }


}
