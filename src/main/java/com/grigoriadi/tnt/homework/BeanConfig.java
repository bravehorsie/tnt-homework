package com.grigoriadi.tnt.homework;

import com.grigoriadi.tnt.homework.client.PricingClient;
import com.grigoriadi.tnt.homework.service.PricingService;
import com.grigoriadi.tnt.homework.service.PricingServiceImpl;
import com.grigoriadi.tnt.homework.aggregation.AggregationWorker;
import com.grigoriadi.tnt.homework.aggregation.AggregationWorkerImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class BeanConfig {


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplateBuilder.setConnectTimeout(Duration.of(10, ChronoUnit.SECONDS));
        restTemplateBuilder.setReadTimeout(Duration.of(10, ChronoUnit.SECONDS));
        return restTemplateBuilder;
    }

}
