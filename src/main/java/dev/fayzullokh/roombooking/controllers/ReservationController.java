package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReservationController {
    private final RoomRepository roomRepository;

    @GetMapping("/rooms")
    public String showAvailableRooms(Model model) {
        List<Room> availableRooms = roomRepository.findByAvailableTrue();
        model.addAttribute("rooms", availableRooms);
        return "rooms";
    }

    @PostMapping("/reserve")
    public String reserveRoom(@RequestParam("roomId") Long roomId,
                              @RequestParam("startDate") LocalDate startDate,
                              @RequestParam("endDate") LocalDate endDate) {
        // process reservation
        return "redirect:/rooms";
    }
}