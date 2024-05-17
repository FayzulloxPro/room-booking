package dev.fayzullokh.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.fayzullokh.roombooking.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class UserResponseDto {

    private String chatId;

    @NotBlank
    @Size(min = 3, max = 50)//if chat id is not null then user is logged in his account in telegram with this chat id
    private String username;

    @Email
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]*$")
    private String phone;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    private Role role;

    private LocalDateTime lastLogin;

    private boolean passwordChanged;
}
