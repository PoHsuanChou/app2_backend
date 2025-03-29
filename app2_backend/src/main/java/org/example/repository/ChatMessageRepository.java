package org.example.repository;

import org.example.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 查找兩個用戶之間的聊天記錄
    List<ChatMessage> findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
            String senderId1, String receiverId1,
            String receiverId2, String senderId2
    );


    // 查找用戶的所有聊天記錄
    List<ChatMessage> findBySenderIdOrReceiverId(String senderId, String to);

    ChatMessage findTopByChatRoomIdOrderByTimestampDesc(String chatRoomId);

    boolean existsBySenderIdOrReceiverId(String senderId, String receiverId);

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

    List<ChatMessage> findBySenderId(String sendId);

    List<ChatMessage> findByReceiverId(String receiveId);

    // New method to optimize the active chat room ID collection
    @Query(value = "{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }", fields = "{ 'chatRoomId': 1 }")
    Set<String> findDistinctChatRoomIdsBySenderIdOrReceiverId(String senderId, String receiverId);

    List<ChatMessage> findBySenderIdIn(List<String> matchUserIds);

    List<ChatMessage> findByReceiverIdIn(List<String> matchUserIds);
}
