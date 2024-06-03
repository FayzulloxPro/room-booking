package dev.fayzullokh.roombooking.utils;

import dev.fayzullokh.roombooking.dtos.BookingDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingValidator {

    public static boolean isValid(BookingDto bookingDto) {
        LocalTime startTime = bookingDto.getStartTime();
        LocalTime endTime = bookingDto.getEndTime();
        LocalDate date = bookingDto.getDate();
        LocalDateTime now = LocalDateTime.now();

        // Validate start and end time
        LocalTime earliestTime = LocalTime.of(9, 0);
        LocalTime latestTime = LocalTime.of(18, 0);

        if (startTime == null || endTime == null || date == null) {
            return false;
        }
        if (startTime.isBefore(earliestTime) || startTime.isAfter(latestTime) ||
                endTime.isBefore(earliestTime) || endTime.isAfter(latestTime) ||
                !startTime.isBefore(endTime)) {
            return false;
        }
        // Validate date is today or within the next 7 days
        LocalDate today = LocalDate.now();
        LocalDate weekFromToday = today.plusDays(7);
        if (date.isBefore(today) || date.isAfter(weekFromToday)) {
            return false;
        }
        // Validate booking is requested at least 1 hour before start time
        LocalDateTime bookingStartDateTime = LocalDateTime.of(date, startTime);

        return !now.plusHours(1).isAfter(bookingStartDateTime);
    }
}
