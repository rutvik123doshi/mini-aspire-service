package com.rutvik.interview.aspire.miniaspireservice.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;


@Configuration
public class CommonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Exclude null values from JSON output
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // Optionally, you can configure other serialization features
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Add more configuration as needed

        return objectMapper;
    }

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizeJackson() {
        return builder -> builder.modulesToInstall(new JavaTimeModule());
    }
}

