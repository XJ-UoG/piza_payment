package com.xj.payment_processor.config;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ResponseStorage {
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<String> waitForResponse(String requestId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        return future;
    }

    public void completeResponse(String requestId, String response) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(response);
        }
    }
}