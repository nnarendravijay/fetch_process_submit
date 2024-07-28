package com.hubspot.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.exception.ApiException;
import com.hubspot.model.CallRecord;
import com.hubspot.model.CallRecords;
import com.hubspot.model.Results;
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

    public CallRecords fetchData() throws IOException {
        logger.info("Fetching data from API");
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(apiUrl) + "/test-dataset?userKey=9d0ec4894762ef3d2b564ae6e80f")
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
                return new CallRecords(Collections.emptyList());
            }

            String bodyString = responseBody.string();
            if (bodyString.isEmpty()) {
                logger.warn("Response body is empty, no updates found.");
                return new CallRecords(Collections.emptyList());
            }

            try {
                CallRecords callRecords = objectMapper.readValue(bodyString, new TypeReference<>() {});
                logger.debug("Deserialization successful: {}", callRecords);
                return callRecords;
            } catch (JsonParseException e) {
                logger.error("Malformed JSON received from API: {}", bodyString, e);
                throw new ApiException("Malformed JSON received from API", e);
            } catch (JsonMappingException e) {
                logger.error("JSON doesn't conform to Data model: {}", bodyString, e);
                throw new ApiException("JSON doesn't conform to Data model", e);
            }
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    public void sendData(Results results) throws IOException {
        logger.info("Sending data to API");
        String jsonData = objectMapper.writeValueAsString(results);

        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(apiUrl) + "/test-result?userKey=9d0ec4894762ef3d2b564ae6e80f") //"/result?userKey=9d0ec4894762ef3d2b564ae6e80f")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            logger.info(response.toString());
            logger.info(response.message());
            if (!response.isSuccessful()) {
                logger.error("Failed to send data: {}", response);
                throw new ApiException("Unexpected response code: " + response.code());
            }
            logger.info("Data sent successfully");
        }
    }
}
