package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public void saveUser(UserRegisterDTO dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Password mismatch");
        }
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .build();
        repository.save(user);
    }
}
