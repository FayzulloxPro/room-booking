package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    public Room create(RoomDto dto) {
        Room save = roomRepository.save(Room.builder()
                .roomNumber(dto.getRoomNumber())
                .description(dto.getDescription())
                .maxSeats(dto.getMaxSeats())
                .minSeats(dto.getMinSeats())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .build());
        return save;
    }

    public List<Room> findAllRooms() {

        return roomRepository.findAll();
    }
}
