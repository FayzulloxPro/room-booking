package dev.fayzullokh.roombooking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "rooms")
public class Room extends BaseEntity {


    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "type")
    private String type;

    @Column(name = "available")
    private boolean available;
    // other fields, constructors, getters, and setters
}