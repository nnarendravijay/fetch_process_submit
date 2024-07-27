package com.hubspot.service;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;

public class RetryInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);

    private final int maxRetries;
    private final long retryDelayMillis;
    private final Set<Integer> retryableStatusCodes;

    public RetryInterceptor(int maxRetries, long retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryableStatusCodes = new HashSet<>();
        initializeRetryableStatusCodes();
    }

    private void initializeRetryableStatusCodes() {
        retryableStatusCodes.add(HttpURLConnection.HTTP_INTERNAL_ERROR);
        retryableStatusCodes.add(HttpURLConnection.HTTP_BAD_GATEWAY);
        retryableStatusCodes.add(HttpURLConnection.HTTP_UNAVAILABLE);
        retryableStatusCodes.add(HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        IOException lastException = null;
        Response response = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                response = chain.proceed(request);

                if (!retryableStatusCodes.contains(response.code())) {
                    return response;
                }
                response.close();

                logger.warn("Retryable HTTP error: {}. Attempt {}/{}", response.code(), attempt + 1, maxRetries);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Connection failure. Attempt {}/{}", attempt + 1, maxRetries, e);
            }

            try {
                Thread.sleep(retryDelayMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Retry interrupted", ie);
            }
        }

        if (response != null) {
            return response;
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new IOException("Failed to execute request after " + maxRetries + " attempts");
    }
}
