package dev.fayzullokh.roombooking.repositories;

import dev.fayzullokh.roombooking.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
