package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.example.dto.ChatMessagePreview;
import org.example.entity.ChatRoom;
import org.example.entity.User;
import org.example.repository.ChatMessageRepository;
import org.example.repository.ChatRoomRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

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
        // Handle null or empty userId case
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContainingAndStartToChatEquals(
                currentUserId, "Y");

        List<ChatMessagePreview> previews = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            // 找到聊天室中另一位用户
            String otherUserId = chatRoom.getParticipantIds().stream()
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (otherUserId == null) continue;

            // 获取该聊天室最后一条消息
            ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByTimestampDesc(chatRoom.getId());

            if (lastMessage == null) continue;

            // 获取另一位用户的信息
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser == null) continue;



            // 构建用户头像URL
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("static/uploads/")
                    .path(otherUser.getPicture())
                    .toUriString();


            // 确定是否轮到当前用户回复
            Boolean yourTurn = lastMessage.getSenderId().equals(otherUserId);

            // 创建预览对象
            ChatMessagePreview preview = ChatMessagePreview.builder()
                    .id(chatRoom.getId())
                    .senderId(otherUserId)
                    .name(otherUser.getUsername())
                    .image(imageUrl)
                    .content(lastMessage.getContent())
                    .type(lastMessage.getType())
                    .timestamp(lastMessage.getTimestamp())
                    .roomNumber(chatRoom.getId())
                    .yourTurn(yourTurn)
                    .build();

            previews.add(preview);
        }

        // 按最新消息时间排序
        previews.sort((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()));

        return previews;

    }
}