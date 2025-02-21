package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.aop.MessageType;
import org.example.dto.MessageReadRequest;
import org.example.dto.MessageReadResponse;
import org.example.entity.ChatMessage;
import org.example.repository.ChatMessageRepository;
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
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, ChatMessage> redisTemplate;

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessage message) {
        message.setTimestamp(new Date());

        // 保存消息到數據庫
        ChatMessage savedMessage = ChatMessage.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())  // 設定接收者 ID
                .content(message.getContent())
                .type(message.getType())  // 確保 MessageType 由客戶端傳入
                .timestamp(new Date())
                .build();

        chatMessageRepository.save(savedMessage);
        // 发布消息到 Redis
        redisTemplate.convertAndSend("chat:private", savedMessage);


        // 發送消息給接收者
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),         // WebSocket 用戶名
                "/queue/private",          // 私人消息隊列
                savedMessage              // 發送保存後的消息
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