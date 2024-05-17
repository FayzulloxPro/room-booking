package dev.fayzullokh.roombooking.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.fayzullokh.roombooking.dtos.UserResponseDto;
import dev.fayzullokh.roombooking.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "users")
public class User extends BaseEntityAudit {

    @Column(name = "chat_id")
    private String chatId;    //if chat id is not null then user is logged in his account in telegram with this chat id
    @Column(name = "username", unique = true, nullable = false, updatable = false)
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password; // hashed
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime lastLogin;

    @Column(name = "password_changed")
    private boolean passwordChanged;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }


    public UserResponseDto toResponseDto() {
        return UserResponseDto.builder()
                .chatId(this.chatId)
                .username(this.username)
                .email(this.email)
                .phone(this.phone)
                .role(this.role)
                .lastLogin(this.lastLogin)
                .passwordChanged(this.passwordChanged)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .build();
    }
}