package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.MessageType;
import org.example.config.LanguageDetector;
import org.example.dto.MessageReadRequest;
import org.example.dto.MessageReadResponse;
import org.example.dto.chat.MessagePayload;
import org.example.entity.*;
import org.example.repository.*;
import org.example.service.GeminiService;
import org.example.service.TarotCardService;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private static final int MAX_API_CALLS = 5;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final UserApiUsageRepository userApiUsageRepository;
    private final GeminiService geminiService;
    private final LanguageDetector languageDetector;
    private final DailyTarotDrawRepository dailyTarotDrawRepository;
    private final TarotCardService tarotCardService;
    private final TarotCardRepository tarotCardRepository;

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessagePayload message) {
        log.info("sendPrivateMessage={} ",message );

        // 保存消息到數據庫
        ChatMessage savedMessage = ChatMessage.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())  // 設定接收者 ID
                .content(message.getContent())
                .chatRoomId(message.getRoomNumber())
                .type(message.getType())  // 確保 MessageType 由客戶端傳入
                .timestamp(new Date())
                .build();

        chatMessageRepository.save(savedMessage);


        //更新last interactionDate
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(message.getRoomNumber());
        if (chatRoom != null) {
            chatRoom.setLastMessageAt(new Date());  // 更新為當前時間
            chatRoomRepository.save(chatRoom);  // 保存更新
        }

        //確定是不是聊過天
        ChatRoom chatRoomForConvo = chatRoomRepository.findByParticipants(message.getSenderId(),message.getReceiverId());
        if(chatRoomForConvo.getStartToChat() == null || "N".equals(chatRoomForConvo.getStartToChat())){
            chatRoomForConvo.setStartToChat("Y");
            chatRoomRepository.save(chatRoomForConvo);
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

    @MessageMapping("/ai")
    public void sendPrivateMessageForAI(@Payload MessagePayload message) {
        log.info("sendPrivateMessage={} ",message );

        log.info("Received message: {}", message);

        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();
        String content = message.getContent();
        String roomNumber = message.getRoomNumber();
        MessageType type = message.getType();

        // 保存用户消息到数据库
        ChatMessage savedMessage = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)  // 设定接收者 ID（AI 场景下可能是 "ai"）
                .content(content)
                .chatRoomId(roomNumber)
                .type(type)  // 确保 MessageType 由客户端传入
                .timestamp(new Date())
                .build();
        chatMessageRepository.save(savedMessage);

        // 檢查當天抽牌記錄
        LocalDate today = LocalDate.now();
        Date startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Optional<DailyTarotDraw> tarotDrawOpt = dailyTarotDrawRepository.findByUserIdAndDateRange(senderId, startDate, endDate);
        if (tarotDrawOpt.isEmpty()) {
            String drawPrompt = languageDetector.detectLanguage(content).equals("zh") ?
                    "您今天尚未抽取塔羅牌，請先抽牌再進行諮詢。" :
                    "You haven't drawn a Tarot card today. Please draw a card before asking.";
            ChatMessage drawMessage = ChatMessage.builder()
                    .senderId("system")
                    .receiverId(senderId)
                    .content(drawPrompt)
                    .chatRoomId(roomNumber)
                    .type(MessageType.TEXT)
                    .timestamp(new Date())
                    .build();
            chatMessageRepository.save(drawMessage);
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + message.getSenderId(), // 使用 senderId 作為 Topic，與前端一致
                    drawMessage
            );
            return;
        }


        // 检查并更新 API 使用次数
        UserApiUsage usage = userApiUsageRepository.findById(senderId)
                .orElse(UserApiUsage.builder()
                        .userId(senderId)
                        .geminiApiCalls(0)
                        .lastResetDate(LocalDate.now())
                        .build());

        // 如果日期不同，重置次数
        if (!today.equals(usage.getLastResetDate())) {
            usage.setGeminiApiCalls(0);
            usage.setLastResetDate(today);
        }



        // 檢查是否超過限制
        if (usage.getGeminiApiCalls() >= MAX_API_CALLS) {
            log.warn("User {} has exceeded the Gemini API limit of {}", senderId, MAX_API_CALLS);
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + message.getSenderId(), // 使用 senderId 作為 Topic，與前端一致
                    ChatMessageAI.builder()
                            .senderId("system")
                            .receiverId(senderId)
                            .content("您今天已達到 Gemini API 使用上限 (5 次)，請明天再試。")
                            .type(MessageType.TEXT)
                            .timestamp(new Date())
                            .build());

            return;
        }
// 獲取塔羅牌資訊
        // 獲取塔羅牌資訊
        DailyTarotDraw tarotDraw = tarotDrawOpt.get();
        Integer cardId = tarotDraw.getCardId();
        Optional<TarotCard> tarotCardOpt = tarotCardRepository.findByNumber(cardId);
        if (tarotCardOpt.isEmpty()) {
            String errorPrompt = languageDetector.detectLanguage(content).equals("zh") ?
                    "無法找到對應的塔羅牌資訊，請聯繫管理員。" :
                    "Could not find the corresponding Tarot card information. Please contact the administrator.";
            ChatMessage errorMessage = ChatMessage.builder()
                    .senderId("system")
                    .receiverId(senderId)
                    .content(errorPrompt)
                    .chatRoomId(roomNumber)
                    .type(MessageType.TEXT)
                    .timestamp(new Date())
                    .build();
            chatMessageRepository.save(errorMessage);
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + message.getSenderId(), // 使用 senderId 作為 Topic，與前端一致
                    errorMessage);
            return;
        }

        TarotCard tarotCard = tarotCardOpt.get();
        String language = languageDetector.detectLanguage(content);
        String cardName = tarotCard.getName();
        String cardDescription = tarotCard.getDescription() != null ? tarotCard.getDescription() : "無詳細描述";

        // 語言格式化（假設 name 是英文，中文需手動翻譯）
        String displayCardName = language.equals("zh") ?
                switch (cardName.toLowerCase()) {
                    case "the fool" -> "愚者";
                    case "the magician" -> "魔術師";
                    case "the high priestess" -> "女祭司";
                    default -> cardName;
                } : cardName;

        // 設定 Prompt
        String systemPrompt = language.equals("zh") ?
                "你是一位塔羅牌專家，僅使用繁體中文回覆，且僅回答與用戶今日抽到的塔羅牌（" + displayCardName + "）相關的問題，例如工作、愛情、健康等牌義解讀。可以一些簡單的問候 然後字數不要多餘 100 個字 但是請拒絕回答與塔羅牌無關的問題。以下是牌的描述供參考：" + cardDescription :
                "You are a Tarot card expert. Answer only in English, and only respond to questions related to the user's drawn Tarot card today (" + cardName + "), such as work, love, or health interpretations. can answer some easy greeting and no more than 100 words Refuse to answer unrelated questions. Card description for reference: " + cardDescription;

        String userPrompt = language.equals("zh") ?
                "我抽到的塔羅牌是 " + displayCardName + "。我的問題是：" + content :
                "My drawn Tarot card is " + cardName + ". My question is: " + content;

        // 呼叫 Gemini API
        String aiResponse = geminiService.generateContent(systemPrompt, userPrompt);


        // 更新 API 调用次数
        usage.setGeminiApiCalls(usage.getGeminiApiCalls() + 1);
        userApiUsageRepository.save(usage);

        // 保存 AI 回复
        ChatMessage aiMessage = ChatMessage.builder()
                .senderId("ai")
                .receiverId(senderId)
                .content(aiResponse)
                .chatRoomId(roomNumber)
                .type(MessageType.TEXT)
                .timestamp(new Date())
                .build();
        chatMessageRepository.save(aiMessage);

        // 也可以單獨發送給接收者 (如果需要)
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getSenderId(), // 使用 senderId 作為 Topic，與前端一致
                aiMessage
        );
    }
}