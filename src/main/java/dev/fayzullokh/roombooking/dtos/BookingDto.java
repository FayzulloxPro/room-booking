package dev.fayzullokh.roombooking.dtos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class BookingDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
    private Long roomId;
    private String code;
    private String comment;
}
