package org.example.repository;

import org.example.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 查找兩個用戶之間的聊天記錄
    List<ChatMessage> findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
            String senderId1, String receiverId1,
            String receiverId2, String senderId2
    );


    // 查找用戶的所有聊天記錄
    List<ChatMessage> findBySenderIdOrReceiverId(String senderId, String to);

    Optional<ChatMessage> findTopByChatRoomIdOrderByTimestampDesc(String chatRoomId);

    boolean existsBySenderIdOrReceiverId(String senderId, String receiverId);

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

    List<ChatMessage> findBySenderId(String sendId);

    List<ChatMessage> findByReceiverId(String receiveId);


}
