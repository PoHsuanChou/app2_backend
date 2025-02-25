package org.example.dto.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.example.aop.MessageType;

@Data
public class MessagePayload {
    private String chatRoomId;
    private String content;
    private String receiverId;
    private String senderId;
    private String timestamp;
    @JsonDeserialize(using = MessageTypeDeserializer.class)
    private MessageType type;

}
