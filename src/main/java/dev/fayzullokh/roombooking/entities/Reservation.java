package dev.fayzullokh.roombooking.entities;

import dev.fayzullokh.roombooking.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntityAudit {


    @Column(nullable = false, name = "reservation_start_time")
    private LocalTime startTime;

    @Column(nullable = false, name = "reservation_end_time")
    private LocalTime endTime;

    @Column(nullable = false, name = "date")
    private LocalDate date;

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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ReservationStatus status;
}
