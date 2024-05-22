package dev.fayzullokh.roombooking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "rooms")
@Where(clause = "deleted = false")
public class Room extends BaseEntityAudit {


    @Column(name = "room_number", unique = true)
    private String roomNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "max_seats")
    private short maxSeats;

    @Column(name = "min_seats")
    private short minSeats;
    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "available", columnDefinition = "boolean default true")
    private boolean available;

}