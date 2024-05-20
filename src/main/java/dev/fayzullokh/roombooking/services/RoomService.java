package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.exceptions.NotFoundException;
import dev.fayzullokh.roombooking.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public RoomDto findById(Long roomId) {
        Optional<Room> byId = roomRepository.findById(roomId);
        if (byId.isEmpty()) {
            throw new NotFoundException("Room not found with id: " + roomId);
        }
        Room room = byId.get();
        return new RoomDto(
                room.getRoomNumber(),
                room.getDescription(),
                room.getMaxSeats(),
                room.getMinSeats(),
                room.getOpenTime(),
                room.getCloseTime()
        );
    }

    public Room update(Long roomId, RoomDto dto) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room is not found with id: " + roomId));

        room.setRoomNumber(dto.getRoomNumber());
        room.setDescription(dto.getDescription());
        room.setMaxSeats(dto.getMaxSeats());
        room.setMinSeats(dto.getMinSeats());
        room.setOpenTime(dto.getOpenTime());
        room.setCloseTime(dto.getCloseTime());

        return roomRepository.save(room);
    }
}
