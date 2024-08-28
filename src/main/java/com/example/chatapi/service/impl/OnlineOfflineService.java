package com.example.chatapi.service.impl;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Conversation;
import com.example.chatapi.entity.User;
import com.example.chatapi.model.*;
import com.example.chatapi.repository.UserRepository;
import com.example.chatapi.service.IOnlineOfflineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
@Service
public class OnlineOfflineService implements IOnlineOfflineService {
    private final Set<UUID> onlineUsers;
    private final Map<UUID, Set<String>> userSubscribed;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    public OnlineOfflineService(
            UserRepository userRepository, SimpMessageSendingOperations simpMessageSendingOperations) {
        this.onlineUsers = new ConcurrentSkipListSet<>();
        this.userSubscribed = new ConcurrentHashMap<>();
        this.userRepository = userRepository;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    public void addOnlineUser(Principal user) {
        if (user == null) return;
        UserDetailsImpl userDetails = getUserDetails(user);
        log.info("{} is online", userDetails.getUsername());
        for (UUID id : onlineUsers) {
            simpMessageSendingOperations.convertAndSend(
                    "/topic/" + id,
                    ChatMessage.builder()
                            .messageType(MessageType.FRIEND_ONLINE)
                            .userConnection(UserConnection.builder().connectionId(userDetails.getId()).build())
                            .build());
        }
        onlineUsers.add(userDetails.getId());
    }

    public void removeOnlineUser(Principal user) {
        if (user != null) {
            UserDetailsImpl userDetails = getUserDetails(user);
            log.info("{} went offline", userDetails.getUsername());
            onlineUsers.remove(userDetails.getId());
            userSubscribed.remove(userDetails.getId());
            for (UUID id : onlineUsers) {
                simpMessageSendingOperations.convertAndSend(
                        "/topic/" + id,
                        ChatMessage.builder()
                                .messageType(MessageType.FRIEND_OFFLINE)
                                .userConnection(UserConnection.builder().connectionId(userDetails.getId()).build())
                                .build());
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        return onlineUsers.contains(userId);
    }

    private UserDetailsImpl getUserDetails(Principal principal) {
        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) principal;
        Object object = user.getPrincipal();
        return (UserDetailsImpl) object;
    }

    public List<UserResponse> getOnlineUsers() {
        return userRepository.findAllById(onlineUsers).stream()
                .map(
                        userEntity ->
                                new UserResponse(
                                        userEntity.getId(), userEntity.getUsername(), userEntity.getEmail()))
                .toList();
    }

    public void addUserSubscribed(Principal user, String subscribedChannel) {
        UserDetailsImpl userDetails = getUserDetails(user);
        log.info("{} subscribed to {}", userDetails.getUsername(), subscribedChannel);
        Set<String> subscriptions = userSubscribed.getOrDefault(userDetails.getId(), new HashSet<>());
        subscriptions.add(subscribedChannel);
        userSubscribed.put(userDetails.getId(), subscriptions);
    }

    public void removeUserSubscribed(Principal user, String subscribedChannel) {
        UserDetailsImpl userDetails = getUserDetails(user);
        log.info("unsubscription! {} unsubscribed {}", userDetails.getUsername(), subscribedChannel);
        Set<String> subscriptions = userSubscribed.getOrDefault(userDetails.getId(), new HashSet<>());
        subscriptions.remove(subscribedChannel);
        userSubscribed.put(userDetails.getId(), subscriptions);
    }

    public boolean isUserSubscribed(UUID username, String subscription) {
        Set<String> subscriptions = userSubscribed.getOrDefault(username, new HashSet<>());
        return subscriptions.contains(subscription);
    }

    public Map<String, Set<String>> getUserSubscribed() {
        Map<String, Set<String>> result = new HashMap<>();
        List<User> users = userRepository.findAllById(userSubscribed.keySet());
        users.forEach(user -> result.put(user.getUsername(), userSubscribed.get(user.getId())));
        return result;
    }

    public void notifySender(
            UUID senderId,
            List<Conversation> entities,
            MessageDeliveryStatusEnum messageDeliveryStatusEnum) {
        if (!isUserOnline(senderId)) {
            log.info(
                    "{} is not online. cannot inform the socket. will persist in database",
                    senderId.toString());
            return;
        }
        List<MessageDeliveryStatusUpdate> messageDeliveryStatusUpdates =
                entities.stream()
                        .map(
                                e ->
                                        MessageDeliveryStatusUpdate.builder()
                                                .id(e.getId())
                                                .messageDeliveryStatusEnum(messageDeliveryStatusEnum)
                                                .content(e.getContent())
                                                .build())
                        .toList();
        for (Conversation entity : entities) {
            simpMessageSendingOperations.convertAndSend(
                    "/topic/" + senderId,
                    ChatMessage.builder()
                            .id(entity.getId())
                            .messageDeliveryStatusUpdates(messageDeliveryStatusUpdates)
                            .messageType(MessageType.MESSAGE_DELIVERY_UPDATE)
                            .content(entity.getContent())
                            .build());
        }
    }
}
