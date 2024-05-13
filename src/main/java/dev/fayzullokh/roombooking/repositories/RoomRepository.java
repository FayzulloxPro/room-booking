package dev.fayzullokh.roombooking.repositories;

import dev.fayzullokh.roombooking.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAvailableTrue();
}