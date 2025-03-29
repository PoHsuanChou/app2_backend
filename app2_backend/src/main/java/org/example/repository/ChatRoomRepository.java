package org.example.repository;

import org.example.entity.ChatRoom;
import org.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByParticipantIdsContains(String userId);

    ChatRoom findChatRoomById(String chatroomId);

    List<ChatRoom> findByParticipantIdsContaining(String userId);

    @Query("{ 'participantIds': { $all: [?0, ?1] } }")
    ChatRoom findByParticipants(String userId, String matchedUserId);

    // New method to find chat rooms where user is a participant and startToChat is null or "N"
    List<ChatRoom> findByParticipantIdsContainingAndStartToChatIsNullOrStartToChatEquals(
            String userId, String startToChatValue);

    List<ChatRoom> findByParticipantIdsContainingAndStartToChatEquals(String userId, String startToChat);
}
