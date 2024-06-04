package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.exceptions.NotFoundException;
import dev.fayzullokh.roombooking.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserService userService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    public Room create(RoomDto dto) {
        Room save = roomRepository.save(Room.builder()
                .roomNumber(dto.getRoomNumber())
                .description(dto.getDescription())
                .maxSeats((short) Math.max(dto.getMaxSeats(), dto.getMinSeats()))
                .minSeats((short) Math.min(dto.getMaxSeats(), dto.getMinSeats()))
                /*.openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())*/
                .available(true)
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
                room.getMinSeats()/*,
                room.getOpenTime(),
                room.getCloseTime()*/
        );
    }

    public Room update(Long roomId, RoomDto dto) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room is not found with id: " + roomId));

        room.setRoomNumber(dto.getRoomNumber());
        room.setDescription(dto.getDescription());
        room.setMaxSeats(dto.getMaxSeats());
        room.setMinSeats(dto.getMinSeats());
        /*room.setOpenTime(dto.getOpenTime());
        room.setCloseTime(dto.getCloseTime());*/

        return roomRepository.save(room);
    }

    private Page<Room> getAllRooms(int page, int size, Long chatId, boolean isTelegramRequest) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("roomNumber").ascending());
        return roomRepository.findAll(pageRequest);
    }

    public Page<Room> getAllRooms(int page, Long chatId, boolean isTelegramRequest) {
        PageRequest pageRequest = PageRequest.of(page, DEFAULT_SIZE, Sort.by("roomNumber").ascending());
        return roomRepository.findAll(pageRequest);
    }

    public Page<Room> getAllRooms(Long chatId, boolean isTelegramRequest) {
        return getAllRooms(DEFAULT_PAGE, DEFAULT_SIZE, chatId, isTelegramRequest);
    }

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room is not found with id: " + roomId));
    }
}
