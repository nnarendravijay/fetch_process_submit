package com.hubspot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.exception.ApiException;
import com.hubspot.model.Data;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApiService {
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final String apiUrl;
    private final String apiKey;

    public ApiService(OkHttpClient client, ObjectMapper objectMapper, String apiUrl, String apiKey) {
        this.client = client.newBuilder().build();
        this.objectMapper = objectMapper.copy();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public List<Data> fetchData() throws IOException {
        logger.info("Fetching data from API");
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(apiUrl))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        ResponseBody responseBody = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to fetch data: {}", response);
                throw new ApiException("Unexpected response code: " + response.code());
            }

            logger.debug("Response received: {}", response);
            responseBody = response.body();
            if (responseBody == null) {
                logger.warn("Response body is null, no updates found.");
                return Collections.emptyList();
            }

            String bodyString = responseBody.string();
            if (bodyString.isEmpty()) {
                logger.warn("Response body is empty, no updates found.");
                return Collections.emptyList();
            }

            try {
                List<Data> dataList = objectMapper.readValue(bodyString,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Data.class));
                logger.debug("Deserialization successful: {}", dataList);
                return dataList;
            } catch (JsonProcessingException e) {
                logger.error("Response from API Service doesn't conform to Data model: {}", bodyString, e);
                return Collections.emptyList();
            }
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    public void sendData(List<Data> data) throws IOException {
        logger.info("Sending data to API");
        String jsonData = objectMapper.writeValueAsString(data);

        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(apiUrl))
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to send data: {}", response);
                throw new ApiException("Unexpected response code: " + response.code());
            }
            logger.info("Data sent successfully");
        }
    }
}
