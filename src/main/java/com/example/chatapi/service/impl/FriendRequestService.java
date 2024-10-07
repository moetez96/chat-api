package com.example.chatapi.service.impl;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import com.example.chatapi.exception.EntityException;
import com.example.chatapi.mapper.FriendRequestResponseMapper;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.dto.FriendRequestResponse;
import com.example.chatapi.model.MessageDeliveryStatusEnum;
import com.example.chatapi.model.MessageType;
import com.example.chatapi.repository.FriendRequestRepository;
import com.example.chatapi.service.IFriendRequestService;
import com.example.chatapi.service.IOnlineOfflineService;
import com.example.chatapi.service.IUserService;
import com.example.chatapi.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FriendRequestService implements IFriendRequestService {

    private final SecurityUtils securityUtils;
    private final IUserService userService;
    private final FriendRequestResponseMapper friendRequestResponseMapper;
    private final IOnlineOfflineService onlineOfflineService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public FriendRequestService(FriendRequestRepository friendRequestRepository,
                                SecurityUtils securityUtils,
                                FriendRequestResponseMapper friendRequestResponseMapper,
                                IOnlineOfflineService onlineOfflineService,
                                SimpMessageSendingOperations simpMessageSendingOperations,
                                IUserService userService) {
        this.friendRequestRepository = friendRequestRepository;
        this.securityUtils = securityUtils;
        this.friendRequestResponseMapper = friendRequestResponseMapper;
        this.onlineOfflineService = onlineOfflineService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.userService = userService;
    }

    public List<FriendRequestResponse> getSentRequests() {
        UUID currentUserId = securityUtils.getUser().getId();
        List<FriendRequest> friendRequests = friendRequestRepository.findFriendRequestBySenderId(currentUserId);
        return friendRequestResponseMapper.toFriendRequestResponses(friendRequests);
    }

    public List<FriendRequestResponse> getReceivedRequests() {
        UUID currentUserId = securityUtils.getUser().getId();
        List<FriendRequest> friendRequests = friendRequestRepository.findFriendRequestByReceiverId(currentUserId);
        return friendRequestResponseMapper.toFriendRequestResponses(friendRequests);
    }

    public List<FriendRequestResponse> getReceivedUnseenRequests() {
        UUID currentUserId = securityUtils.getUser().getId();
        List<FriendRequest> friendRequests = friendRequestRepository.findFriendRequestUnseen(currentUserId);
        return friendRequestResponseMapper.toFriendRequestResponses(friendRequests);
    }

    public void seeFriendsRequests() {
        UUID currentUserId = securityUtils.getUser().getId();
        List<FriendRequest> friendRequests = friendRequestRepository.findFriendRequestUnseen(currentUserId);
        friendRequests.forEach(
                request -> request.setDeliveryStatus(MessageDeliveryStatusEnum.SEEN.toString()));
        List<FriendRequest> saved = friendRequestRepository.saveAll(friendRequests);
        friendRequestResponseMapper.toFriendRequestResponses(saved);
    }

    @Override
    public FriendRequestResponse getReceivedRequestIds(UUID senderId, UUID receiverId) {
        FriendRequest friendRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(senderId, receiverId);
        return friendRequestResponseMapper.toFriendRequestResponse(friendRequest);
    }

    @Transactional
    public FriendRequestResponse addFriendRequest(UUID friendId) {
        UUID currentUserId = securityUtils.getUser().getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User friend = userService.getUserById(friendId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        if (userService.areAlreadyFriends(user.getId(), friend.getId())) {
            throw new EntityException("This user is already in your friends list.");
        }

        if (friendRequestRepository.existsBySenderIdAndReceiverId(user.getId(), friend.getId())) {
            throw new EntityException("You have already sent a friend request to this user.");
        }

        FriendRequest friendRequest;

        boolean isTargetOnline = onlineOfflineService.isUserOnline(friend.getId());
        if (isTargetOnline) {

            ChatMessage chatMessage = ChatMessage.builder()
                    .senderId(currentUserId)
                    .senderUsername(user.getUsername())
                    .receiverId(friend.getId())
                    .receiverUsername(friend.getUsername())
                    .messageType(MessageType.FRIEND_REQUEST)
                    .time(Timestamp.from(Instant.now()))
                    .build();

            friendRequest = FriendRequest.builder()
                    .sender(user)
                    .receiver(friend)
                    .deliveryStatus(MessageDeliveryStatusEnum.DELIVERED.toString())
                    .build();

            log.info("{} is online. FRIEND_REQUEST notification is sent to {}", friend.getUsername(), chatMessage.getReceiverUsername());
            simpMessageSendingOperations.convertAndSend("/topic/" + friend.getId(), chatMessage);
        } else {

            friendRequest = FriendRequest.builder()
                    .sender(user)
                    .receiver(friend)
                    .deliveryStatus(MessageDeliveryStatusEnum.NOT_DELIVERED.toString())
                    .build();

            log.info("{} is offline. FRIEND_REQUEST will be pending.", friend.getUsername());
        }

        friendRequest = friendRequestRepository.save(friendRequest);
        return friendRequestResponseMapper.toFriendRequestResponse(friendRequest);
    }

    @Transactional
    public void acceptFriendRequest(UUID senderId) {
        UUID currentUserId = securityUtils.getUser().getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        Optional<FriendRequest> friendRequest = Optional.ofNullable(friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), user.getId()));

        if (friendRequest.isEmpty()) {
            throw new EntityException("No friend request found between these users.");
        }

        if (userService.areAlreadyFriends(user.getId(), sender.getId())) {
            friendRequestRepository.delete(friendRequest.get());
            throw new EntityException("This user is already in your friends list.");
        }

        user.getFriends().add(sender);
        sender.getFriends().add(user);

        userService.addUser(user);
        userService.addUser(sender);

        friendRequestRepository.delete(friendRequest.get());

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(currentUserId)
                .senderUsername(user.getUsername())
                .receiverId(sender.getId())
                .receiverUsername(sender.getUsername())
                .messageType(MessageType.FRIEND_REQUEST_ACCEPTED)
                .time(friendRequest.get().getCreatedAt())
                .build();

        boolean isTargetOnline = onlineOfflineService.isUserOnline(sender.getId());
        if (isTargetOnline) {

            log.info("{} is online. FRIEND_REQUEST_ACCEPTED notification is sent to {}", sender.getUsername(), chatMessage.getReceiverUsername());
            simpMessageSendingOperations.convertAndSend("/topic/" + sender.getId(), chatMessage);
        } else {
            log.info("{} is offline. FRIEND_REQUEST_ACCEPTED will be pending.", sender.getUsername());
        }
    }

    @Transactional
    public void declineFriendRequest(UUID senderId) {
        UUID currentUserId = securityUtils.getUser().getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        Optional<FriendRequest> friendRequest = Optional.ofNullable(friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), user.getId()));

        if (friendRequest.isEmpty()) {
            throw new EntityException("No friend request found between these users.");
        }

        friendRequestRepository.delete(friendRequest.get());

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(currentUserId)
                .senderUsername(user.getUsername())
                .receiverId(sender.getId())
                .receiverUsername(sender.getUsername())
                .messageType(MessageType.FRIEND_REQUEST_DECLINED)
                .time(friendRequest.get().getCreatedAt())
                .build();

        boolean isTargetOnline = onlineOfflineService.isUserOnline(sender.getId());
        if (isTargetOnline) {

            log.info("{} is online. FRIEND_REQUEST_DECLINED notification is sent to {}", sender.getUsername(), chatMessage.getReceiverUsername());
            simpMessageSendingOperations.convertAndSend("/topic/" + sender.getId(), chatMessage);
        } else {
            log.info("{} is offline. FRIEND_REQUEST_DECLINED will be pending.", sender.getUsername());
        }

        simpMessageSendingOperations.convertAndSend("/topic/" + user.getId(), chatMessage);

    }

    @Transactional
    public void cancelFriendRequest(UUID friendId) {
        UUID currentUserId = securityUtils.getUser().getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User friend = userService.getUserById(friendId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        Optional<FriendRequest> friendRequest = Optional.ofNullable(friendRequestRepository.findBySenderIdAndReceiverId(user.getId(), friend.getId()));

        if (friendRequest.isEmpty()) {
            throw new EntityException("No friend request found between these users.");
        }

        friendRequestRepository.delete(friendRequest.get());

        boolean isTargetOnline = onlineOfflineService.isUserOnline(friend.getId());
        if (isTargetOnline) {
            ChatMessage chatMessage = ChatMessage.builder()
                    .senderId(currentUserId)
                    .senderUsername(user.getUsername())
                    .receiverId(friend.getId())
                    .receiverUsername(friend.getUsername())
                    .messageType(MessageType.FRIEND_REQUEST_CANCELED)
                    .time(friendRequest.get().getCreatedAt())
                    .build();

            log.info("{} is online. FRIEND_REQUEST_CANCELED notification is sent to {}", friend.getUsername(), chatMessage.getReceiverUsername());
            simpMessageSendingOperations.convertAndSend("/topic/" + friend.getId(), chatMessage);
        } else {
            log.info("{} is offline. FRIEND_REQUEST_CANCELED will be pending.", friend.getUsername());
        }
    }
}
