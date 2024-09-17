package com.example.chatapi.mapper;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Conversation;
import com.example.chatapi.entity.User;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.MessageDeliveryStatusEnum;
import com.example.chatapi.model.MessageType;
import com.example.chatapi.service.IUserService;
import com.example.chatapi.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ChatMessageMapper {

    private final IUserService userService;

    @Autowired
    public ChatMessageMapper(UserService userService) {
        this.userService = userService;
    }

    public List<ChatMessage> toChatMessages(
            List<Conversation> conversationEntities,
            UserDetailsImpl userDetails,
            MessageType messageType) {
        List<UUID> fromUsersIds =
                conversationEntities.stream().map(Conversation::getFromUser).toList();
        Map<UUID, String> fromUserIdsToUsername =
                userService.getAllByIds(fromUsersIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername));

        return conversationEntities.stream()
                .map(e -> toChatMessage(e, userDetails, fromUserIdsToUsername, messageType, MessageDeliveryStatusEnum.valueOf(e.getDeliveryStatus())))
                .toList();
    }

    public ChatMessage toChatMessage(
            Conversation e,
            UserDetailsImpl userDetails,
            Map<UUID, String> fromUserIdsToUsername,
            MessageType messageType,
            MessageDeliveryStatusEnum messageDeliveryStatusEnum) {
        return ChatMessage.builder()
                .id(e.getId())
                .messageType(messageType)
                .content(e.getContent())
                .receiverId(e.getToUser())
                .receiverUsername(userDetails.getUsername())
                .senderId(e.getFromUser())
                .senderUsername(fromUserIdsToUsername.get(e.getFromUser()))
                .messageDeliveryStatusEnum(messageDeliveryStatusEnum)
                .time(e.getTime())
                .build();
    }
}
