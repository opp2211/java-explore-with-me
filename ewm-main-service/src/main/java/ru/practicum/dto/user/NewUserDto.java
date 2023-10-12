package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserDto {
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank
    @Email(message = "Invalid email")
    @Size(min = 6, max = 254)
    private String email;
}
