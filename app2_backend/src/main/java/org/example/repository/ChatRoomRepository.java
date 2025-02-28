package org.example.repository;

import org.example.entity.ChatRoom;
import org.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByParticipantIdsContains(String userId);

    List<ChatRoom> findByParticipantIdsContaining(String userId);
}
