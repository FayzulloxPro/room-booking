package dev.fayzullokh.roombooking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.repositories.RoomRepository;
import dev.fayzullokh.roombooking.services.RoomService;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserService userService;
    private final RoomService roomService;
    private final RoomRepository roomRepository;

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

    @Bean
    public CommandLineRunner createRooms() {
        return (args) -> {
            List<Room> all = roomRepository.findAll();
            if (all.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule
                Resource resource = new ClassPathResource("rooms.json");
                InputStream inputStream = resource.getInputStream();
                try {
                    List<RoomDto> roomDtoList = objectMapper.readValue(inputStream,
                            TypeFactory.defaultInstance().constructCollectionType(List.class, RoomDto.class));
                    for (RoomDto roomDto : roomDtoList) {
                        roomService.create(roomDto);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
