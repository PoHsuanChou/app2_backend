package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.example.entity.ChatMessagePreview;
import org.example.entity.ChatRoom;
import org.example.entity.User;
import org.example.repository.ChatMessageRepository;
import org.example.repository.ChatRoomRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
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
        // Handle null or empty userId case
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Find all chat rooms where user participates
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContaining(currentUserId);
        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Get chat room IDs with actual messages (optimization: combine sender/receiver queries)
        Set<String> activeChatRoomIds = chatMessageRepository.findDistinctChatRoomIdsBySenderIdOrReceiverId(
                currentUserId, currentUserId);

        // 3. Build previews
        List<ChatMessagePreview> previews = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            // Skip if not a two-person chat or no messages
            if (chatRoom.getParticipantIds().size() != 2 ||
                    !activeChatRoomIds.contains(chatRoom.getId())) {
                continue;
            }

            // Get the latest message
            ChatMessage lastMessage = chatMessageRepository
                    .findTopByChatRoomIdOrderByTimestampDesc(chatRoom.getId())
                    .orElse(null);

            if (lastMessage != null) {
                String otherUserId = chatRoom.getParticipantIds().stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst()
                        .orElse(null);

                if (otherUserId != null) {
                    previews.add(createPreview(lastMessage, otherUserId, currentUserId, chatRoom.getId()));
                }
            }
        }

        // 4. Sort by timestamp (descending)
        previews.sort(Comparator.comparing(ChatMessagePreview::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return previews;
    }

    /**
     * 創建聊天預覽對象
     */
    private ChatMessagePreview createPreview(ChatMessage lastMessage, String otherUserId,
                                             String currentUserId, String chatRoomId) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + otherUserId));

        String name = otherUser.getNickName() != null ?
                otherUser.getNickName() : "Unknown";
        String image = otherUser.getPicture() != null ?
                otherUser.getPicture() : "default.png";

        // Use profile image if available and picture is null
        if (image.equals("default.png") && otherUser.getProfile() != null &&
                otherUser.getProfile().getProfileImage() != null) {
            image = otherUser.getProfile().getProfileImage();
        }

        return ChatMessagePreview.builder()
                .id(chatRoomId)
                .senderId(otherUserId)
                .name(name)
                .image(image)
                .content(lastMessage.getContent())
                .type(lastMessage.getType())
                .timestamp(lastMessage.getTimestamp())
                .yourTurn(!lastMessage.getSenderId().equals(currentUserId))
                .build();
    }
}