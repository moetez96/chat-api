package com.example.chatapi.service.impl;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Conversation;
import com.example.chatapi.entity.User;
import com.example.chatapi.exception.EntityException;
import com.example.chatapi.mapper.ChatMessageMapper;
import com.example.chatapi.model.*;
import com.example.chatapi.repository.ConversationRepository;
import com.example.chatapi.repository.UserRepository;
import com.example.chatapi.service.IConversationService;
import com.example.chatapi.service.IOnlineOfflineService;
import com.example.chatapi.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@Slf4j
public class ConversationService implements IConversationService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ChatMessageMapper chatMessageMapper;
    private final ConversationRepository conversationRepository;
    private final IOnlineOfflineService onlineOfflineService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    public ConversationService(
            UserRepository userRepository,
            SecurityUtils securityUtils,
            ChatMessageMapper chatMessageMapper,
            ConversationRepository conversationRepository,
            OnlineOfflineService onlineOfflineService,
            SimpMessageSendingOperations simpMessageSendingOperations) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.chatMessageMapper = chatMessageMapper;
        this.conversationRepository = conversationRepository;
        this.onlineOfflineService = onlineOfflineService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    public List<UserConnection> getUserFriends() {
        UserDetailsImpl userDetails = securityUtils.getUser();
        String username = userDetails.getUsername();
        List<User> users = userRepository.findAll();
        User thisUser =
                users.stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst()
                        .orElseThrow(EntityException::new);

        return users.stream()
                .filter(user -> !user.getUsername().equals(username))
                .map(
                        user ->
                                UserConnection.builder()
                                        .connectionId(user.getId())
                                        .connectionUsername(user.getUsername())
                                        .convId(getConvId(user, thisUser))
                                        .unSeen(0)
                                        .isOnline(onlineOfflineService.isUserOnline(user.getId()))
                                        .build())
                .toList();
    }

    public List<UnseenMessageCountResponse> getUnseenMessageCount() {
        List<UnseenMessageCountResponse> result = new ArrayList<>();
        UserDetailsImpl userDetails = securityUtils.getUser();
        List<Conversation> unseenMessages =
                conversationRepository.findUnseenMessagesCount(userDetails.getId());

        if (!CollectionUtils.isEmpty(unseenMessages)) {
            Map<UUID, List<Conversation>> unseenMessageCountByUser = new HashMap<>();
            for (Conversation conversation : unseenMessages) {
                List<Conversation> values =
                        unseenMessageCountByUser.getOrDefault(conversation.getFromUser(), new ArrayList<>());
                values.add(conversation);
                unseenMessageCountByUser.put(conversation.getFromUser(), values);
            }
            log.info("there are some unseen messages for {}", userDetails.getUsername());
            unseenMessageCountByUser.forEach(
                    (user, entities) -> {
                        result.add(
                                UnseenMessageCountResponse.builder()
                                        .count((long) entities.size())
                                        .fromUser(user)
                                        .build());
                        updateMessageDelivery(user, entities, MessageDeliveryStatusEnum.DELIVERED);
                    });
        }
        return result;
    }

    public ChatMessage getLastMessage(String convId) {
        Optional<Conversation> optionalConversation = conversationRepository.findLastMessage(convId);

        return optionalConversation.map(conversation ->
                ChatMessage.builder()
                        .id(conversation.getId())
                        .content(conversation.getContent())
                        .receiverId(conversation.getToUser())
                        .receiverUsername(securityUtils.getUser().getUsername())
                        .senderId(conversation.getFromUser())
                        .messageDeliveryStatusEnum(MessageDeliveryStatusEnum.valueOf(conversation.getDeliveryStatus()))
                        .time(conversation.getTime())
                        .build()
        ).orElse(null);
    }


    private void updateMessageDelivery(
            UUID user,
            List<Conversation> entities,
            MessageDeliveryStatusEnum messageDeliveryStatusEnum) {
        entities.forEach(e -> e.setDeliveryStatus(messageDeliveryStatusEnum.toString()));
        onlineOfflineService.notifySender(user, entities, messageDeliveryStatusEnum);
        conversationRepository.saveAll(entities);
    }

    private void updateMessageDeliveryToUsers(
            String convId,
            List<Conversation> entities) {
        entities.forEach(e -> e.setDeliveryStatus(MessageDeliveryStatusEnum.SEEN.toString()));
        onlineOfflineService.notifyUsers(convId, entities, MessageDeliveryStatusEnum.SEEN);
        conversationRepository.saveAll(entities);
    }

    public List<ChatMessage> setReadMessages(List<ChatMessage> chatMessages) {
        List<UUID> inTransitMessageIds = chatMessages.stream().map(ChatMessage::getId).toList();
        List<Conversation> conversationEntities =
                conversationRepository.findAllById(inTransitMessageIds);
        conversationEntities.forEach(
                message -> message.setDeliveryStatus(MessageDeliveryStatusEnum.SEEN.toString()));
        List<Conversation> saved = conversationRepository.saveAll(conversationEntities);

        saved.stream().findFirst().ifPresent(conversation -> {
            String convId = conversation.getConvId();
            if (convId != null) {
                updateMessageDeliveryToUsers(convId, saved);
            }
        });

        return chatMessageMapper.toChatMessages(saved, securityUtils.getUser(), MessageType.CHAT);
    }

    public List<ChatMessage> getConversationMessages(String convId) {
        List<ChatMessage> result = new ArrayList<>();
        UserDetailsImpl userDetails = securityUtils.getUser();
        List<Conversation> unseenMessages =
                conversationRepository.findConversationMessages(convId);

        if (!CollectionUtils.isEmpty(unseenMessages)) {
            result = chatMessageMapper.toChatMessages(unseenMessages, userDetails, MessageType.CHAT);
        }
        return result;
    }

    public static String getConvId(User user1, User user2) {
        String id1 = user1.getId().toString();
        String id2 = user2.getId().toString();

        return id1.compareTo(id2) > 0 ? id2 + "_" + id1 : id1 + "_" + id2;
    }
}
