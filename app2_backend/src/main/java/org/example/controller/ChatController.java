package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessage message) {
        System.out.println("ggggg" + message);
        messagingTemplate.convertAndSendToUser(
                message.getTo(), // 接收者的用戶名
                "/queue/messages", // 目標隊列
                message // 要發送的消息
        );
    }

    @MessageMapping("/chat.register")
    @SendTo("/topic/public") //表示返回的消息會被廣播到
    public ChatMessage register(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getFrom()); //將用戶名存儲在 WebSocket session 中
        return message;
    }
}