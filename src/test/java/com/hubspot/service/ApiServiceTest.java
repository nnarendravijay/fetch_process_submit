package com.hubspot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.model.Data;
import okhttp3.OkHttpClient;
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

public class ApiServiceTest {

    private static final int TIMEOUT_SECONDS = 10;
    private MockWebServer mockWebServer;
    private ApiService apiService;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OkHttpClient client = new OkHttpClient.Builder()
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
    public void testFetchData() throws IOException {
        // Enqueue a successful response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("[{\"userId\": 1, \"id\": 1, \"title\": \"title1\", \"body\": \"body1\"}]"));

        List<Data> dataList = apiService.fetchData();
        assertNotNull(dataList);
        assertFalse(dataList.isEmpty(), "Data list should not be empty");
        assertEquals(1, dataList.getFirst().id());
    }

    @Test
    public void testSendData() throws IOException {
        // Enqueue a successful response for sending data
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("Data sent successfully"));

        List<Data> dataList = List.of(new Data(1, 1, "title1", "body1"), new Data(1, 2, "title2", "body2"));
        apiService.sendData(dataList);

        // Assert that the request was received by the mock server
        assertEquals(1, mockWebServer.getRequestCount());
    }
}
