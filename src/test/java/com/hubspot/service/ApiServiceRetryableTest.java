package com.hubspot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.model.Data;
import com.hubspot.util.Utils;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ApiServiceRetryableTest {

    private MockWebServer mockWebServer;
    private ApiService apiService;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MILLIS = 1000L;
    private static final int TIMEOUT_SECONDS = 10;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(MAX_RETRIES, RETRY_DELAY_MILLIS))
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        apiService = new ApiService(client, new ObjectMapper(), mockWebServer.url("/").toString(), "dummy_api_key");
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testFetchDataWithRetries() throws IOException {
        // Enqueue 2 failed responses and 1 successful response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("[{\"userId\": 1, \"id\": 1, \"title\": \"title1\", \"body\": \"body1\"}]"));

        // Test fetchData method
        List<Data> dataList = apiService.fetchData();
        assertNotNull(dataList);
        assertEquals(1, dataList.size());
        assertEquals(1, dataList.get(0).id());
    }
}
