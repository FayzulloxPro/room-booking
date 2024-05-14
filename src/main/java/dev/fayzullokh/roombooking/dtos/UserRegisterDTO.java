package dev.fayzullokh.roombooking.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class UserRegisterDTO {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
    private String email;


    public String getUsername() {
        return username.toLowerCase();
    }

    public String getEmail() {
        return email.toLowerCase();
    }
}
