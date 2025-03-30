package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aop.MessageType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessagePreview {
    private String id;
    private String senderId;
    private String name;       // 新增：對應前端 name
    private String image;      // 新增：對應前端 image
    private String content;
    private MessageType type;
    private Date timestamp;
    private String roomNumber;
    private Boolean yourTurn;  // 新增：對應前端 yourTurn
}
