package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.example.entity.ChatMessagePreview;
import org.example.entity.ChatRoom;
import org.example.entity.User;
import org.example.repository.ChatMessageRepository;
import org.example.repository.ChatRoomRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessagePreviewService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 獲取當前用戶的聊天預覽列表
     */
    public List<ChatMessagePreview> getChatPreviews(String currentUserId) {
        // 查找用戶參與的聊天室
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContains(currentUserId);

        List<ChatMessagePreview> previews = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            // 找到該聊天室的最後一條訊息
            ChatMessage lastMessage = chatMessageRepository
                    .findTopByChatRoomIdOrderByTimestampDesc(chatRoom.getId())
                    .orElse(null);

            if (lastMessage != null) {
                previews.add(convertToPreview(lastMessage, currentUserId));
            }
        }

        return previews;
    }

    /**
     * 將 ChatMessage 轉換為 ChatMessagePreview
     */
    private ChatMessagePreview convertToPreview(ChatMessage chatMessage, String currentUserId) {
        User sender = userRepository.findById(chatMessage.getSenderId()).orElse(null);

        return ChatMessagePreview.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .name(sender != null ? sender.getNickName() : "Unknown")
                .image(sender != null ? sender.getPicture() : null)
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .timestamp(chatMessage.getTimestamp())
                .yourTurn(!chatMessage.getSenderId().equals(currentUserId)) // 確認當前用戶是否該回覆
                .build();
    }
}
