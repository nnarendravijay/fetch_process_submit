package com.hubspot;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    private static final int TOTAL_REQUESTS = 4;
    private MockWebServer mockWebServer;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testRun() throws IOException {
        // Enqueue 2 failed responses and 1 successful response
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("[{\"userId\": 1, \"id\": 1, \"title\": \"title1\", \"body\": \"body1\"}]"));
        // Enqueue response for sendData()
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("Data sent successfully"));

        // Redirect standard output to capture logger output for assertions
        new App().run(mockWebServer.url("/").toString(), "dummy_api_key");
        assertEquals(TOTAL_REQUESTS, mockWebServer.getRequestCount(), "Expected three requests (2 retries + 1 successful) for fetchData and 1 for sendData");
    }
}
