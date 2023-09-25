package com.rutvik.interview.aspire.miniaspireservice.api.utils;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ThreadUtil {

    private final ExecutorService executorService;

    public <T,R> CompletableFuture<R> getCompletableFuture(Function<T,R> function, T t) {
        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        return CompletableFuture.supplyAsync(() -> {
            if (Objects.nonNull(mdcContextMap)) {
                MDC.setContextMap(mdcContextMap);
            }
           try {
               return function.apply(t);
           } finally {
               MDC.clear();
           }
        }, executorService);
    }
}
