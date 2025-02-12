package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.example.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;  // 注入聊天记录存储库

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessage message) {
        System.out.println("Received message: " + message);

        // 将消息保存到数据库
        message.setTimestamp(new Date());
        chatMessageRepository.save(message); // 保存消息到数据库

        // 发送消息到接收者
        messagingTemplate.convertAndSendToUser(
                message.getTo(), // 接收者的用户名
                "/queue/messages", // 目标队列
                message // 要发送的消息
        );
    }

    @MessageMapping("/chat.register")
    @SendTo("/topic/public") //表示返回的消息會被廣播到
    public ChatMessage register(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getFrom()); //將用戶名存儲在 WebSocket session 中
        return message;
    }
    // 查询历史消息
    @MessageMapping("/history")
    @SendTo("/topic/public")
    public List<ChatMessage> getChatHistory(@Payload String username) {
        return chatMessageRepository.findByTo(username); // 查询某个用户的历史消息
    }
}