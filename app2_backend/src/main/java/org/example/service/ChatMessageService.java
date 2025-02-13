package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.ChatMessage;
import org.example.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    final private ChatMessageRepository chatMessageRepository;


    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }
}
