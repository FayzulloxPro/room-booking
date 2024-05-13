package dev.fayzullokh.roombooking.entities;

import dev.fayzullokh.roombooking.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "users")
public class User extends BaseEntity {


    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "password")
    private String password; // hashed
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;

    private Role role;

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}