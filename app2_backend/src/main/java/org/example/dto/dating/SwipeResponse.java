package org.example.dto.dating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SwipeResponse {
    private boolean success;
    private boolean isMatch;
    private String chatRoomId;
}
