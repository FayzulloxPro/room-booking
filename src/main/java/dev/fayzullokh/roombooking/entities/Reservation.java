package dev.fayzullokh.roombooking.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "reservations")
public class Reservation extends BaseEntityAudit {

    @Column(nullable = false, name = "seat_number")
    private short seatNumber;

    @Column(nullable = false, name = "reservation_start_time")
    private LocalDateTime reservationStartTime;

    @Column(nullable = false, name = "reservation_end_time")
    private LocalDateTime reservationEndTime;

    @JoinColumn(nullable = false, name = "responsible_user_id")
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private User responsibleUser;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<User> users;
    @JoinColumn(nullable = false)
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Room room;

    @Column(nullable = false, columnDefinition = "boolean default false", name = "is_expired")
    private boolean isExpired;
}
