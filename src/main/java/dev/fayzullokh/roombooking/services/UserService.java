package dev.fayzullokh.roombooking.services;

import dev.fayzullokh.roombooking.config.SessionUser;
import dev.fayzullokh.roombooking.dtos.ChangePasswordRequestDto;
import dev.fayzullokh.roombooking.dtos.UserRegisterDTO;
import dev.fayzullokh.roombooking.dtos.UserResponseDto;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.enums.Role;
import dev.fayzullokh.roombooking.exceptions.AccessDeniedException;
import dev.fayzullokh.roombooking.exceptions.NotFoundException;
import dev.fayzullokh.roombooking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SessionUser sessionUser;

    public User saveUser(UserRegisterDTO dto, String chatId, boolean isTelegramRequest) {
        if (isTelegramRequest) {
            if (chatId == null) {
                throw new RuntimeException("Chat id is null");
            }
            User userInSession = userRepository.findByUsername(chatId).orElseThrow(() -> new NotFoundException("User not found with chat id: %s".formatted(chatId)));
            if (userInSession.getRole() == Role.USER) {
                throw new AccessDeniedException("Access denied for user with chat id: %s".formatted(chatId));
            }
            User build = User.builder()
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .email(dto.getEmail())
                    .role(Role.USER)
                    .build();

            build.setCreatedBy(userInSession.getUsername());
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Password mismatch");
        }
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User saveUser(UserRegisterDTO dto) {
        return saveUser(dto, "", false);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean isPasswordChanged(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && user.get().isPasswordChanged();
    }

    public User changePassword(String username, ChangePasswordRequestDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: %s".formatted(username)));

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        if (!user.getPassword().equals(passwordEncoder.encode(dto.getOldPassword()))) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordChanged(true);
        return userRepository.save(user);
    }

    public Page<UserResponseDto> getAll(Pageable pageable) {
        Page<User> all = userRepository.findAll(pageable);
        if (all.getContent().size() == 0) {
            return null;
        }
        return all.map(user -> UserResponseDto.builder()
                .chatId(user.getChatId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .passwordChanged(user.isPasswordChanged())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build());
    }

    public boolean updateProfile(UserResponseDto dto) {
        User user = sessionUser.getUser();
        if (user == null) {
            return false;
        }
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        userRepository.save(user);
        return true;
    }

    public User getUserByChatId(long chatId, boolean isTelegramRequest) {
        if (isTelegramRequest) {
            return userRepository.findByChatId(chatId)
                    .orElse(null);
        }
        return null;
    }

    public User login(long chatId, String text, boolean isTelegramRequest) {
        if (isTelegramRequest) {
            String[] split = text.split("#");
            if (split.length != 2) {
                return null;
            }
            String username = split[0];
            String password = split[1];
            User user = getUser(username);
            if (Objects.isNull(user)) {
                return null;
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Successful login and make this telegram user as logged in to account
                user.setChatId(chatId);
                userRepository.save(user);
                return user;
            }

        }
        //hozircha
        return null;
    }

    public User logout(long chatId, boolean isTelegramRequest) {
        if (isTelegramRequest) {
            User userByChatId = getUserByChatId(chatId, true);
            if (Objects.isNull(userByChatId)) {
                return null;
            }
            userByChatId.setChatId(null);
            userRepository.save(userByChatId);
            return userByChatId;
        }
        return null;
    }

    /*public User logout(long chatId, boolean isTelegramRequest) {
        if(isTelegramRequest){
            User userByChatId = getUserByChatId(chatId, true);
            userByChatId.
        }
    }*/
}
