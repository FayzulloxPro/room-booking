package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.config.SessionUser;
import dev.fayzullokh.roombooking.dtos.UserResponseDto;
import dev.fayzullokh.roombooking.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView updateProfile(@ModelAttribute("dto") UserResponseDto dto, BindingResult result) {
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView("user/profile");
            mav.addObject("user", dto);
            mav.addObject("errors", result.getAllErrors());
            return mav;
        }

        boolean updated = userService.updateProfile(dto);
        return new ModelAndView("redirect:/users/profile");
    }

    @PostMapping("/profile")
    public ModelAndView postProfile(@Valid @ModelAttribute("user") UserResponseDto dto, BindingResult result, HttpServletRequest request) {
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView("user/profile");
            mav.addObject("user", dto);
            return mav;
        }

        boolean updated = userService.updateProfile(dto);
        return new ModelAndView("redirect:/users/profile");
    }

}
