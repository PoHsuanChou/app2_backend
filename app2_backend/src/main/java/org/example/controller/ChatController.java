package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.MessageType;
import org.example.dto.MessageReadRequest;
import org.example.dto.MessageReadResponse;
import org.example.dto.chat.MessagePayload;
import org.example.entity.ChatMessage;
import org.example.entity.Match;
import org.example.repository.ChatMessageRepository;
import org.example.repository.MatchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final MatchRepository matchRepository;

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessagePayload message) {
        log.info("sendPrivateMessage={} ",message );

        // 保存消息到數據庫
        ChatMessage savedMessage = ChatMessage.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())  // 設定接收者 ID
                .content(message.getContent())
                .type(message.getType())  // 確保 MessageType 由客戶端傳入
                .timestamp(new Date())
                .chatRoomId(message.getChatRoomId())
                .build();

        chatMessageRepository.save(savedMessage);


        //更新last interactionDate
        Match match = matchRepository.findByUserIds(message.getSenderId(), message.getReceiverId());
        if (match != null) {
            match.setLastInteractionDate(new Date());  // 更新為當前時間
            matchRepository.save(match);  // 保存更新
        }





        // 发布消息到 Redis
        // 发布消息到 Redis
        redisTemplate.convertAndSend("chat:private", savedMessage);


//        // 發送消息到聊天室 - 所有訂閱該聊天室的用戶都會收到
//        messagingTemplate.convertAndSend(
//                "/topic/chat/" + message.getChatRoomId(),
//                savedMessage
//        );

        // 也可以單獨發送給接收者 (如果需要)
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/queue/private",
                savedMessage
        );
    }

    // 新增方法：獲取聊天歷史記錄
    @MessageMapping("/get-chat-history")
    public void getChatHistory(String chatRoomId, SimpMessageHeaderAccessor headerAccessor) {
        // Get the user ID from the WebSocket session
        String userId = headerAccessor.getUser().getName(); // Assuming you've set the user principal

        log.info("Received request for chat history. ChatRoomId: {}, UserId: {}", chatRoomId, userId);

        // Fetch chat history from the database
        List<ChatMessage> chatHistory = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);

        log.info("Found {} messages for chatRoomId: {}", chatHistory.size(), chatRoomId);
        log.info("for chatRoomId: {} chatHistory={}",chatRoomId,chatHistory);

        // Send the chat history to the user
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/chat-history",
                chatHistory
        );
    }

    @MessageMapping("/chat.register")
    @SendTo("/topic/public")
    public ChatMessage register(
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 確保 username 不為空
        if (message.getSenderId() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSenderId());
        }
        return message;
    }

    // 獲取聊天歷史記錄
    @GetMapping("/api/chat/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String userId,
            @RequestParam(required = false) String withUserId
    ) {
        List<ChatMessage> messages;
        if (withUserId != null) {
            // 獲取兩個用戶之間的聊天記錄
            messages = chatMessageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
                    userId, withUserId, withUserId, userId
            );
        } else {
            // 獲取用戶的所有聊天記錄
            messages = chatMessageRepository.findBySenderIdOrReceiverId(userId, userId);
        }
        return ResponseEntity.ok(messages);
    }

    // 標記消息為已讀
    @MessageMapping("/message.read")
    public void markMessageAsRead(@Payload MessageReadRequest request) {
        chatMessageRepository.findById(request.getMessageId())
                .ifPresent(message -> {
                    chatMessageRepository.save(message);

                    // 通知發送者消息已讀
                    messagingTemplate.convertAndSendToUser(
                            message.getSenderId(),
                            "/queue/message-read",
                            MessageReadResponse.builder()
                                    .messageId(message.getId())
                                    .build()
                    );
                });
    }
}