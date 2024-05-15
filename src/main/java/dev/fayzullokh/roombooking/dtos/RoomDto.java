package dev.fayzullokh.roombooking.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class RoomDto {

    @NotBlank(message = "Room number cannot be blank")
    private String roomNumber;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Min(value = 1, message = "Max seats must be at least 1")
    private short maxSeats;

    @Min(value = 1, message = "Min seats must be at least 1")
    private short minSeats;

    @NotNull(message = "Open time must be specified")
    private LocalTime openTime;

    @NotNull(message = "Close time must be specified")
    private LocalTime closeTime;
}