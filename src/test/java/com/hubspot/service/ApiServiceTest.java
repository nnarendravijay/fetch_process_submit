package com.hubspot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.model.Data;
import com.hubspot.util.Utils;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApiServiceTest {

    private static final int TIMEOUT_SECONDS = 10;

    @Test
    public void testFetchData() throws IOException {
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String apiUrl = Utils.getEnvCaseInsensitive("API_URL");
        final String apiKey = Utils.getEnvCaseInsensitive("API_KEY");
        assertNotNull(apiUrl, "API_URL environment variable is not set. Please set it to the API endpoint URL.");
        assertTrue(!apiUrl.isEmpty(), "API_URL environment variable is empty. Please set it to the API endpoint URL.");

        assertNotNull(apiKey, "API_KEY environment variable is not set. Please set it to your API key.");
        assertTrue(!apiKey.isEmpty(), "API_KEY environment variable is empty. Please set it to your API key.");

        ApiService apiService = new ApiService(client, objectMapper, apiUrl, apiKey);
        List<Data> dataList = apiService.fetchData();
        assertNotNull(dataList);
        assertFalse(dataList.isEmpty(), "Data list should not be empty");
    }

    @Test
    public void testSendData() throws IOException {
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String apiUrl = Utils.getEnvCaseInsensitive("API_URL");
        final String apiKey = Utils.getEnvCaseInsensitive("API_KEY");
        assertNotNull(apiUrl, "API_URL environment variable is not set. Please set it to the API endpoint URL.");
        assertTrue(!apiUrl.isEmpty(), "API_URL environment variable is empty. Please set it to the API endpoint URL.");

        assertNotNull(apiKey, "API_KEY environment variable is not set. Please set it to your API key.");
        assertTrue(!apiKey.isEmpty(), "API_KEY environment variable is empty. Please set it to your API key.");

        ApiService apiService = new ApiService(client, objectMapper, apiUrl, apiKey);
        List<Data> dataList = List.of(new Data(1, 1, "title1", "body1"), new Data(1, 2, "title2", "body2"));
        apiService.sendData(dataList);
    }
}
