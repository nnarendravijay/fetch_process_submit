package com.hubspot.model;

import java.time.*;

public record CallRecord(
        int customerId,
        String callId,
        long startTimestamp,
        long endTimestamp
) {
//    public LocalDateTime getStartTime() {
//        LocalDate date = LocalDate.ofEpochDay(Duration.ofMillis(startTimestamp).toDays());
//        LocalTime time = LocalTime.ofNanoOfDay(Duration.ofMillis(startTimestamp).toNanosPart());
//        return LocalDateTime.of(date, time);
//    }
//
//    public LocalDateTime getEndTime() {
//        LocalDate date = LocalDate.ofEpochDay(Duration.ofMillis(endTimestamp).toDays());
//        LocalTime time = LocalTime.ofNanoOfDay(Duration.ofMillis(endTimestamp).toNanosPart());
//        return LocalDateTime.of(date, time);
//    }

    public LocalDateTime getStartTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneId.systemDefault());
    }

    public LocalDateTime getEndTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneId.systemDefault());
    }

    public Duration getDuration() {
        return Duration.between(getStartTime(), getEndTime());
    }

}
