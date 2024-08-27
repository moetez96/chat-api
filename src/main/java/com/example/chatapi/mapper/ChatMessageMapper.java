package com.example.chatapi.mapper;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Conversation;
import com.example.chatapi.entity.User;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.MessageType;
import com.example.chatapi.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ChatMessageMapper {

    private final UserRepository userRepository;

    public ChatMessageMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<ChatMessage> toChatMessages(
            List<Conversation> conversationEntities,
            UserDetailsImpl userDetails,
            MessageType messageType) {
        List<UUID> fromUsersIds =
                conversationEntities.stream().map(Conversation::getFromUser).toList();
        Map<UUID, String> fromUserIdsToUsername =
                userRepository.findAllById(fromUsersIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername));

        return conversationEntities.stream()
                .map(e -> toChatMessage(e, userDetails, fromUserIdsToUsername, messageType))
                .toList();
    }

    private static ChatMessage toChatMessage(
            Conversation e,
            UserDetailsImpl userDetails,
            Map<UUID, String> fromUserIdsToUsername,
            MessageType messageType) {
        return ChatMessage.builder()
                .id(e.getId())
                .messageType(messageType)
                .content(e.getContent())
                .receiverId(e.getToUser())
                .receiverUsername(userDetails.getUsername())
                .senderId(e.getFromUser())
                .senderUsername(fromUserIdsToUsername.get(e.getFromUser()))
                .build();
    }
}
