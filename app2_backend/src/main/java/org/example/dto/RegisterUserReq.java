package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.entity.Birthday;
import org.example.dto.entity.SelectedCard;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserReq {

    private Birthday birthday;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    @NotBlank(message = "Nickname cannot be blank")
    @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
    private String nickname;

    @NotNull(message = "Selected card cannot be null")
    private SelectedCard selectedCard;
}
