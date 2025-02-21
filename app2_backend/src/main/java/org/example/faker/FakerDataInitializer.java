package org.example.faker;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.example.ChatRoomStatus;
import org.example.MatchStatus;
import org.example.aop.MessageType;
import org.example.entity.ChatMessage;
import org.example.entity.ChatRoom;
import org.example.entity.Match;
import org.example.entity.User;
import org.example.repository.ChatMessageRepository;
import org.example.repository.ChatRoomRepository;
import org.example.repository.MatchRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile("dev")  // 只在 dev 環境執行，避免污染正式環境
@RequiredArgsConstructor
public class FakerDataInitializer implements CommandLineRunner {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final Faker faker = new Faker();

    private static final String TARGET_USER_ID = "67b5fc1a53f27d64b537651c";
    private static final List<String> OTHER_USER = List.of("67b5fc1a53f27d64b537651d","67b5fc1a53f27d64b537651e","67b5fc1a53f27d64b537651f");
    @Override
    public void run(String... args) throws Exception {
        createMatches();
        createChat();
    }

    private void createMatches() {
        if (matchRepository.count() > 0) {
            System.out.println("已有數據，跳過初始化");
            return;
        }

        List<Match> matches = List.of(
                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651d")
                        .matchScore(85.0)
                        .status(MatchStatus.ACCEPTED)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build(),

                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651e")
                        .matchScore(90.0)
                        .status(MatchStatus.PENDING)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build(),

                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651f")
                        .matchScore(75.0)
                        .status(MatchStatus.REJECTED)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build()
        );

        matchRepository.saveAll(matches);
        System.out.println("✅ 測試配對數據初始化完成！");

    }
    private void createChat(){
        if (chatMessageRepository.existsBySenderIdOrReceiverId(TARGET_USER_ID, TARGET_USER_ID)) {
            System.out.println("目標用戶已有聊天數據，跳過初始化");
            return;
        }

        List<User> users = userRepository.findAll();
        if (users.size() < 2) {
            System.out.println("用戶數不足，無法創建聊天數據");
            return;
        }
        List<ChatRoom> chatRooms = new ArrayList<>();
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // 避免匹配自己
            User otherUser;
            do {
                otherUser = users.get(faker.number().numberBetween(0, users.size()));
            } while (otherUser.getId().equals(TARGET_USER_ID));

            // 創建聊天房間
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setParticipantIds(List.of(TARGET_USER_ID, otherUser.getId()));
            chatRoom.setStatus(ChatRoomStatus.ACTIVE);
            chatRoom.setCreatedAt(new Date());
            chatRoom.setLastMessageAt(new Date());
            chatRoomRepository.save(chatRoom);
            chatRooms.add(chatRoom);

            // 創建 5 則訊息
            for (int j = 0; j < 5; j++) {
                String senderId = faker.bool().bool() ? TARGET_USER_ID : otherUser.getId();
                ChatMessage chatMessage = ChatMessage.builder()
                        .chatRoomId(chatRoom.getId())
                        .senderId(senderId)
                        .receiverId(senderId.equals(TARGET_USER_ID) ? otherUser.getId() : TARGET_USER_ID)
                        .content(faker.lorem().sentence())
                        .type(MessageType.TEXT)
                        .timestamp(faker.date().past(30, TimeUnit.DAYS))
                        .build();

                chatMessages.add(chatMessage);
            }
        }

        chatRoomRepository.saveAll(chatRooms);
        chatMessageRepository.saveAll(chatMessages);

        System.out.println("成功為目標用戶建立 3 間聊天房間，每間 5 則訊息！");

    }
}
