package dev.fayzullokh.roombooking.config;

import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserService userService;

    @Bean
    public CommandLineRunner createAdmin() {
        return (args) -> {
            User user1 = userService.getUser("admin");
            if (user1 != null) return;
            User user = userService.saveUser(new UserRegisterDTO(
                    "admin",
                    "admin",
                    "admin",
                    "admin"
            ));
            log.info("Admin created: {}", user);
        };
    }
    @Bean
    public CommandLineRunner createUser() {
        return (args) -> {
            User user1 = userService.getUser("fayzullokh");
            if (user1 != null) return;
            User user = userService.saveUser(new UserRegisterDTO(
                    "fayzullokh",
                    "123",
                    "123",
                    "fayzullokh@mail.ru"
            ));
            log.info("User created: {}", user);
        };
    }
}
