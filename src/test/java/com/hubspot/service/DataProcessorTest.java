//package com.hubspot.service;
//
//import com.hubspot.model.Data;
//import org.junit.jupiter.api.Test;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class DataProcessorTest {
//
//    private static final double EXPECTED_AVERAGE = 1.5;
//    private static final double DELTA = 0.01;
//
//    @Test
//    public void testCalculateAverage() {
//        DataProcessor processor = new DataProcessor();
//
//        List<Data> dataList = Arrays.asList(
//                new Data(1, 1, "title1", "body1"),
//                new Data(2, 2, "title1", "body1")
//        );
//
//        double average = processor.calculateAverage(dataList);
//        assertEquals(EXPECTED_AVERAGE, average, DELTA);
//    }
//}
