package com.hubspot.model;

import java.util.List;

public record Result(
        int customerId,
        String date,
        int maxConcurrentCalls,
        List<String> callIds,
        long timestamp
) {}
