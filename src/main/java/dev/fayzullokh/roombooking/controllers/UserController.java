package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.config.SessionUser;
import dev.fayzullokh.roombooking.dtos.UserResponseDto;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SessionUser sessionUser;

    @GetMapping
    public ModelAndView getAllUsers(ModelAndView modelAndView, Pageable pageable) {
        Page<UserResponseDto> users = userService.getAll(pageable);
        modelAndView.setViewName("user/users");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @GetMapping("/profile")
    public ModelAndView profile(ModelAndView mav) {
        mav.addObject("user", sessionUser.getUser().toResponseDto());
        mav.setViewName("user/profile");

        return mav;
    }

    @PutMapping("/profile")
    public boolean updateProfile(@Valid UserResponseDto dto){
        return userService.updateProfile(dto);
    }
}
