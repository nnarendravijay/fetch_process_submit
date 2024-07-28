package com.hubspot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.exception.ApiException;
import com.hubspot.model.CallRecord;
import com.hubspot.model.CallRecords;
import com.hubspot.model.Result;
import com.hubspot.model.Results;
import com.hubspot.service.ApiService;
import com.hubspot.service.DataProcessor;
import com.hubspot.service.RetryInterceptor;
import com.hubspot.util.Utils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO: Based on use case, we could implement this as a daemon using connection pooling, mTLS for efficiency and security
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MILLIS = 2000;

    public static void main(String[] args) {
        final String apiUrl = Utils.getEnvCaseInsensitive("API_URL");
        final String apiKey = Utils.getEnvCaseInsensitive("API_KEY");
        if (apiUrl == null || apiUrl.isEmpty()) {
            logger.error("API_URL environment variable is not set. Please set it to the API endpoint URL.");
            System.exit(1);
        }
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("API_KEY environment variable is not set. Please set it to your API key.");
            System.exit(1);
        }
        new App().run(apiUrl, apiKey);
    }

    void run(String apiUrl, String apiKey) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(MAX_RETRIES, RETRY_DELAY_MILLIS))
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        ApiService apiService = new ApiService(client, new ObjectMapper(), apiUrl, apiKey);
        DataProcessor dataProcessor = new DataProcessor();

        try {
            logger.info("Starting application");

            CallRecords callRecords = apiService.fetchData();
            if (callRecords.callRecords().isEmpty()) {
                return;
            }
            List<Result> results = DataProcessor.findMaxConcurrentCallsPerCustomerPerDay(callRecords.callRecords());
            logger.info(results.toString());
            Results resultsWrapper = new Results(results);

//            logger.info("Max concurrent calls per customer per day" + resultsWrapper);

            apiService.sendData(resultsWrapper);
            logger.info("Data sent successfully");
        } catch (ApiException e) {
            logger.error("An API error occurred", e);
            throw new RuntimeException("Application encountered a critical API error", e);
        } catch (IOException e) {
            logger.error("An I/O error occurred", e);
            throw new RuntimeException("Application encountered a critical I/O error", e);
        }
    }
}
