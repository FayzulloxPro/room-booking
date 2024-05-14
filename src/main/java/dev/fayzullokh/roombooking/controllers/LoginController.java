package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping( "/auth" )
public class LoginController {

    private final UserService userService;

    @GetMapping( "/register" )
    public String registerPage(Model model) {
        model.addAttribute("dto", new UserRegisterDTO());
        return "registration";
    }
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

    @PostMapping( "/register" )
    public String register(@Valid @ModelAttribute UserRegisterDTO dto, BindingResult result ) {
        if ( result.hasErrors() ) {
            return "registration";
        }

        if ( !dto.getPassword().equals(dto.getConfirmPassword()) ) {
            result.rejectValue("confirmPassword", "", "Passwords do not match");
            return "registration";
        }

        userService.saveUser(dto);

        return "success";
    }
}