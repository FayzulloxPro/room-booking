package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.dtos.BookingDto;
import dev.fayzullokh.roombooking.entities.Reservation;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.enums.ReservationStatus;
import dev.fayzullokh.roombooking.exceptions.CustomIllegalArgumentException;
import dev.fayzullokh.roombooking.repositories.ReservationRepository;
import dev.fayzullokh.roombooking.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final UserService userService;

    public Reservation create(BookingDto bookingDto, long chatId) {
        Room roomById = roomService.getRoomById(bookingDto.getRoomId());
        if (Objects.isNull(roomById)) {
            throw new CustomIllegalArgumentException("Room not found");
        }
        LocalDate date = bookingDto.getDate();
        if (date.isAfter(LocalDate.now(ZoneId.of("Asia/Tashkent")).plusDays(7))) {
            throw new CustomIllegalArgumentException("Date should be less than 7 days from now");
        }
        LocalTime startTime = bookingDto.getStartTime();
        int startTimeHour = startTime.getHour();
        LocalTime endTime = bookingDto.getEndTime();
        int endTimeHour = endTime.getHour();
        if (startTimeHour > endTimeHour) {
            bookingDto.setStartTime(endTime);
            bookingDto.setEndTime(startTime);
        } else if (startTimeHour == endTimeHour) {
            int startTimeMinute = startTime.getMinute();
            int endTimeMinute = endTime.getMinute();
            if (startTimeMinute > endTimeMinute) {
                bookingDto.setStartTime(endTime);
                bookingDto.setEndTime(startTime);
            }
        }

        Reservation reservation = Reservation.builder()
                .startTime(bookingDto.getStartTime())
                .endTime(bookingDto.getEndTime())
                .date(bookingDto.getDate())
                .responsibleUser(userService.getUserByChatId(chatId, true))
                .room(roomById)
                .isExpired(false)
                .status(ReservationStatus.IN_PROGRESS)
                .build();
        return reservationRepository.save(reservation);
    }
}
