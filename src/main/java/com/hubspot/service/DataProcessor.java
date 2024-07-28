package com.hubspot.service;

import com.hubspot.model.CallRecord;
import com.hubspot.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    private static String formatDate(long epoch) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(epoch));
    }

    public static List<Result> findMaxConcurrentCallsPerCustomerPerDay(List<CallRecord> calls) {
        Map<String, List<CallRecord>> callsByCustomerAndDay = new HashMap<>();

        for (CallRecord call : calls) {
            long startTimestamp = call.startTimestamp();
            long endTimestamp = call.endTimestamp();

            String startDate = formatDate(startTimestamp);
            String endDate = formatDate(endTimestamp);

            if (startDate.equals(endDate)) {
                // Call does not span multiple days
                String key = call.customerId() + "_" + startDate;
                callsByCustomerAndDay.computeIfAbsent(key, k -> new ArrayList<>()).add(call);
            } else {
                // Call spans multiple days
                long currentStartTimestamp = startTimestamp;
                while (currentStartTimestamp < endTimestamp) {
                    String currentDate = formatDate(currentStartTimestamp);
                    long nextMidnightTimestamp = getNextMidnightTimestamp(currentStartTimestamp);

                    long currentEndTimestamp = Math.min(endTimestamp, nextMidnightTimestamp);

                    // Exclude calls ending exactly at midnight of the next day
                    if (currentEndTimestamp == nextMidnightTimestamp) {
                        currentEndTimestamp -= 1;
                    }

                    CallRecord splitCall = new CallRecord(call.customerId(), call.callId(), currentStartTimestamp, currentEndTimestamp);
                    String key = call.customerId() + "_" + currentDate;
                    callsByCustomerAndDay.computeIfAbsent(key, k -> new ArrayList<>()).add(splitCall);

                    currentStartTimestamp = nextMidnightTimestamp;
                    if (currentStartTimestamp == endTimestamp) {
                        break;
                    }
                }
            }
        }

        List<Result> results = new ArrayList<>();

        for (Map.Entry<String, List<CallRecord>> entry : callsByCustomerAndDay.entrySet()) {
            List<CallRecord> dayCalls = entry.getValue();
            dayCalls.sort(Comparator.comparingLong(CallRecord::startTimestamp));

            Queue<CallRecord> pq = new PriorityQueue<>(Comparator.comparingLong(CallRecord::endTimestamp));
            int maxConcurrentCalls = 0;
            long timestamp = 0;
            List<String> callIds = new ArrayList<>();

            for (CallRecord call : dayCalls) {
                while (!pq.isEmpty() && pq.peek().endTimestamp() <= call.startTimestamp()) {
                    pq.poll();
                }
                pq.offer(call);
                if (pq.size() > maxConcurrentCalls) {
                    maxConcurrentCalls = pq.size();
                    callIds.clear();
                    callIds.addAll(pq.stream().map(CallRecord::callId).toList());
                    timestamp = call.startTimestamp();
                }
            }

            String[] keyParts = entry.getKey().split("_");
            int customerId = Integer.parseInt(keyParts[0]);
            String date = keyParts[1];

            results.add(new Result(customerId, date, maxConcurrentCalls, callIds, timestamp));
        }

        // Sort the results by date
        results.sort(Comparator.comparing(Result::date));
        return results;
    }

    private static long getNextMidnightTimestamp(long currentTimestamp) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(currentTimestamp);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
