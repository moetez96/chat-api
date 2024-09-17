package com.example.chatapi.service.impl;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Conversation;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.MessageDeliveryStatusEnum;
import com.example.chatapi.repository.ConversationRepository;
import com.example.chatapi.service.IChatService;
import com.example.chatapi.service.IOnlineOfflineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ChatService implements IChatService {

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    private final ConversationRepository conversationRepository;

    private final IOnlineOfflineService onlineOfflineService;

    @Autowired
    public ChatService(
            SimpMessageSendingOperations simpMessageSendingOperations,
            ConversationRepository conversationRepository,
            OnlineOfflineService onlineOfflineService) {
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.conversationRepository = conversationRepository;
        this.onlineOfflineService = onlineOfflineService;
    }

    public void sendMessageToConvId(
            ChatMessage chatMessage, String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        UserDetailsImpl userDetails = getUser();
        UUID fromUserId = userDetails.getId();
        UUID toUserId = chatMessage.getReceiverId();
        populateContext(chatMessage, userDetails);
        boolean isTargetOnline = onlineOfflineService.isUserOnline(toUserId);
        boolean isTargetSubscribed =
                onlineOfflineService.isUserSubscribed(toUserId, "/topic/" + conversationId);
        chatMessage.setId(UUID.randomUUID());

        Conversation.ConversationBuilder conversationBuilder =
                Conversation.builder();

        conversationBuilder
                .id(chatMessage.getId())
                .fromUser(fromUserId)
                .toUser(toUserId)
                .content(chatMessage.getContent())
                .convId(conversationId);
        if (!isTargetOnline) {
            log.info(
                    "{} is not online. Content saved in unseen messages", chatMessage.getReceiverUsername());
            conversationBuilder.deliveryStatus(MessageDeliveryStatusEnum.NOT_DELIVERED.toString());
            chatMessage.setMessageDeliveryStatusEnum(MessageDeliveryStatusEnum.NOT_DELIVERED);
            simpMessageSendingOperations.convertAndSend("/topic/" + fromUserId.toString(), chatMessage);
        } else if (!isTargetSubscribed) {
            log.info(
                    "{} is online but not subscribed. sending to their private subscription",
                    chatMessage.getReceiverUsername());
            conversationBuilder.deliveryStatus(MessageDeliveryStatusEnum.DELIVERED.toString());
            chatMessage.setMessageDeliveryStatusEnum(MessageDeliveryStatusEnum.DELIVERED);
            simpMessageSendingOperations.convertAndSend("/topic/" + toUserId.toString(), chatMessage);
            simpMessageSendingOperations.convertAndSend("/topic/" + fromUserId.toString(), chatMessage);

        } else {
            conversationBuilder.deliveryStatus(MessageDeliveryStatusEnum.SEEN.toString());
            chatMessage.setMessageDeliveryStatusEnum(MessageDeliveryStatusEnum.SEEN);
            simpMessageSendingOperations.convertAndSend("/topic/" + toUserId.toString(), chatMessage);
            simpMessageSendingOperations.convertAndSend("/topic/" + fromUserId.toString(), chatMessage);
        }
        conversationRepository.save(conversationBuilder.build());
        simpMessageSendingOperations.convertAndSend("/topic/" + conversationId, chatMessage);
    }

    private void populateContext(ChatMessage chatMessage, UserDetailsImpl userDetails) {
        chatMessage.setSenderUsername(userDetails.getUsername());
        chatMessage.setSenderId(userDetails.getId());
    }

    public UserDetailsImpl getUser() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (UserDetailsImpl) object;
    }
}
