package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aop.MessageType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "chatMessagesAI")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageAI {
    @Id
    private String id;
    private String chatRoomId; // 關聯 ChatRoom
    private String senderId;
    private String receiverId;
    private String content;
    private MessageType type;
    private Date timestamp;
}
