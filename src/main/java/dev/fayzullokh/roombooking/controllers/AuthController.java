package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping( "/auth" )
public class AuthController {

    private final UserService userService;

    @GetMapping( "/login" )
    public ModelAndView loginPage(@RequestParam( required = false ) String error ) {
        var mav = new ModelAndView();
        mav.addObject("error", error);
        mav.setViewName("login");
        return mav;
    }

    @GetMapping( "/logout" )
    public ModelAndView logoutPage() {
        var mav = new ModelAndView();
        mav.setViewName("logout");
        return mav;
    }


    @GetMapping( "/register" )
    public String registerPage(Model model) {
        model.addAttribute("dto", new UserRegisterDTO());
        return "registration";
    }
    @PostMapping( "/register" )
    @PreAuthorize("hasRole('ADMIN')")
    public String register(@Valid @ModelAttribute UserRegisterDTO dto, BindingResult result ) {
        if ( result.hasErrors() ) {
            return "registration";
        }
        userService.saveUser(dto);
        return "success";
    }
}