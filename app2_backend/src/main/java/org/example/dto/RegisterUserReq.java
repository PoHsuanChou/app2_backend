package org.example.dto;

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
    private String email;
    private String gender;
    private String nickname;
    private SelectedCard selectedCard;
}
