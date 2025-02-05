package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

    @DBRef
    private List<User> participants;

    private String status;  // "active", "archived"
    private Date createdAt;
    private Date lastMessageAt;
}
