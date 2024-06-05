package dev.fayzullokh.roombooking.dtos;

import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class BookingDto {
    private Long roomId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
    private String comment;
}
