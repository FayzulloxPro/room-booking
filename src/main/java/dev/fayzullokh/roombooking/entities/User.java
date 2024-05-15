package dev.fayzullokh.roombooking.entities;

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

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

}