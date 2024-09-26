package com.example.chatapi.service.impl;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import com.example.chatapi.exception.EntityException;
import com.example.chatapi.mapper.FriendRequestResponseMapper;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.dto.FriendRequestResponse;
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

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(user)
                .receiver(friend)
                .build();

        log.info("User {} is sending a friend request to {}", user.getUsername(), friend.getUsername());
        friendRequest = friendRequestRepository.save(friendRequest);
        log.info("Friend request saved: {} -> {}. Request ID: {}", user.getUsername(), friend.getUsername(), friendRequest.getId());

        boolean isTargetOnline = onlineOfflineService.isUserOnline(friend.getId());
        if (isTargetOnline) {
            ChatMessage chatMessage = ChatMessage.builder()
                    .senderId(currentUserId)
                    .senderUsername(user.getUsername())
                    .receiverId(friend.getId())
                    .receiverUsername(friend.getUsername())
                    .messageType(MessageType.FRIEND_REQUEST)
                    .time(friendRequest.getCreatedAt())
                    .build();

            log.info("{} is online. Request notification is sent to {}", friend.getUsername(), chatMessage.getReceiverUsername());
            simpMessageSendingOperations.convertAndSend("/topic/" + friend.getId(), chatMessage);
        } else {
            log.info("{} is offline. Friend request will be pending.", friend.getUsername());
        }

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
    }
}
