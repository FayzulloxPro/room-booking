package dev.fayzullokh.roombooking.controllers;

import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;



    // allow only authenticated users
    @GetMapping("/home")
    public ModelAndView home(Principal principal) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUser(principal!=null?principal.getName():null);
        modelAndView.addObject("user", user);
        modelAndView.setViewName("home/index");
        return modelAndView;
    }
    @GetMapping("/")
    public ModelAndView mainPage(Principal principal) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUser(principal!=null?principal.getName():null);
        modelAndView.addObject("user", user);
        modelAndView.setViewName("home/index");
        return modelAndView;
    }
}