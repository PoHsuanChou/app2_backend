package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ChatRoomStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "chatRooms")
@Data
@NoArgsConstructor
public class ChatRoom {
    @Id
    private String id;

    @DBRef
    private Match match;

    private List<String> participantIds; // 只存 userId，避免 @DBRef

    @Indexed
    private ChatRoomStatus status;  // 使用枚舉

    @Indexed
    private Date createdAt;
    private Date lastMessageAt;
}
