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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        // 查找用戶參與的所有聊天室
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContaining(currentUserId);

        // 查找用戶發送的所有消息的聊天室ID
        List<String> sentMessageChatRoomIds = chatMessageRepository.findBySenderId(currentUserId)
                .stream()
                .map(ChatMessage::getChatRoomId)
                .distinct()
                .collect(Collectors.toList());

        // 查找用戶接收的所有消息的聊天室ID
        List<String> receivedMessageChatRoomIds = chatMessageRepository.findByReceiverId(currentUserId)
                .stream()
                .map(ChatMessage::getChatRoomId)
                .distinct()
                .collect(Collectors.toList());

        // 合併所有有消息的聊天室ID
        Set<String> activeChatRoomIds = new HashSet<>();
        activeChatRoomIds.addAll(sentMessageChatRoomIds);
        activeChatRoomIds.addAll(receivedMessageChatRoomIds);

        List<ChatMessagePreview> previews = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            // 只處理雙人聊天且有消息記錄的聊天室
            if (chatRoom.getParticipantIds().size() != 2 || !activeChatRoomIds.contains(chatRoom.getId())) {
                continue;
            }

            // 找到該聊天室的最後一條訊息
            ChatMessage lastMessage = chatMessageRepository
                    .findTopByChatRoomIdOrderByTimestampDesc(chatRoom.getId())
                    .orElse(null);

            if (lastMessage != null) {
                // 獲取對話的另一方用戶ID
                String otherUserId = chatRoom.getParticipantIds().stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst()
                        .orElse(null);

                if (otherUserId != null) {
                    previews.add(createPreview(lastMessage, otherUserId, currentUserId, chatRoom.getId()));
                }
            }
        }

        // 按最新消息時間戳排序
        previews.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return previews;
    }

    /**
     * 創建聊天預覽對象
     */
    private ChatMessagePreview createPreview(ChatMessage lastMessage, String otherUserId,
                                             String currentUserId, String chatRoomId) {
        // 獲取對話對象的信息
        User otherUser = userRepository.findById(otherUserId).orElse(null);

        return ChatMessagePreview.builder()
                .id(chatRoomId)  // 使用聊天室ID而不是消息ID
                .senderId(otherUserId)  // 顯示對話對象的ID，不是最後發消息的人
                .name(otherUser != null ? otherUser.getNickName() : "Unknown")
                .image(otherUser != null ? otherUser.getPicture() : "default.png")
                .content(lastMessage.getContent())
                .type(lastMessage.getType())
                .timestamp(lastMessage.getTimestamp())
                .yourTurn(!lastMessage.getSenderId().equals(currentUserId))  // 如果最後發消息的不是當前用戶，則輪到他回复
                .build();
    }
}