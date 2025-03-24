package org.example.dto.dating;

import lombok.Data;

@Data
public class SwipeRequest {
    private String userId;
    private String targetUserId;
    private String action;
}
