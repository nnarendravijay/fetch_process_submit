package com.hubspot.service;

import com.hubspot.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    public double calculateAverage(List<Data> dataList) {
        logger.info("Calculating average from data list");
        double average = dataList.stream()
                .mapToInt(Data::id)
                .average()
                .orElse(0.0);
        logger.info("Calculated average: {}", average);
        return average;
    }
}
