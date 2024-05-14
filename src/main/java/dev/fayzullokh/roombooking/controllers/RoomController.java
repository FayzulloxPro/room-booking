package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.services.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView create(@Valid @ModelAttribute("roomDto") RoomDto dto, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("room/create"); // Show the form with validation errors
            return modelAndView;
        }
        Room room = roomService.create(dto);
        modelAndView.setViewName("redirect:/rooms"); // Redirect to another page after successful creation
        return modelAndView;
    }

    @GetMapping()
    public ModelAndView showAvailableRooms(ModelAndView model) {
        List<Room> availableRooms = roomService.findAllRooms();
        model.setViewName("room/rooms");
        model.addObject("rooms", availableRooms);
        model.addObject("roomDto", new RoomDto());
        return model;
    }
}
