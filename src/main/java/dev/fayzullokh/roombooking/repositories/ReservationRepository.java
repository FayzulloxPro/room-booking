package dev.fayzullokh.roombooking.repositories;

import dev.fayzullokh.roombooking.entities.Reservation;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND r.date = :date " +
            "AND (r.startTime < :endTime AND r.endTime > :startTime)")
    List<Reservation> findOverlappingReservations(@Param("roomId") Long roomId,
                                                  @Param("date") LocalDate date,
                                                  @Param("startTime") LocalTime startTime,
                                                  @Param("endTime") LocalTime endTime);

    @Query("SELECT r FROM Reservation r WHERE r.responsibleUser = :user AND r.status = :status AND r.date BETWEEN CURRENT_DATE AND CURRENT_DATE + :days AND r.deleted = false ORDER BY r.date DESC")
    List<Reservation> getUserReservationsInXDays(User user, ReservationStatus status, Integer days);

    List<Reservation> findAllByRoomId(Long roomId);
}
