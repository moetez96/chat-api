package com.example.chatapi.service.impl;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import com.example.chatapi.exception.EntityException;
import com.example.chatapi.mapper.FriendRequestResponseMapper;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.FriendRequestResponse;
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

import java.util.List;
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
                                UserService userService) {
        this.friendRequestRepository = friendRequestRepository;
        this.securityUtils = securityUtils;
        this.friendRequestResponseMapper = friendRequestResponseMapper;
        this.onlineOfflineService = onlineOfflineService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.userService = userService;
    }

    public List<FriendRequest> getUserRequests() {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();

        return friendRequestRepository.findFriendRequestBySenderId(currentUserId).stream().toList();

    }

    public List<FriendRequestResponse> getFriendsRequests() {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();
        List<FriendRequest> friendRequests = friendRequestRepository.findFriendRequestByReceiverId(currentUserId).stream().toList();
        return friendRequestResponseMapper.toFriendRequestResponses(friendRequests);
    }

    public FriendRequestResponse addFriendRequest(UUID friendId) {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();

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

        FriendRequest.FriendRequestBuilder friendRequestBuilder = FriendRequest.builder();
        FriendRequest friendRequest = friendRequestBuilder
                .sender(user).receiver(friend).build();

        log.info("User {} is sending a friend request to {}", user.getUsername(), friend.getUsername());
        friendRequest = friendRequestRepository.save(friendRequest);
        log.info("Friend request saved: {} -> {}", user.getUsername(), friend.getUsername());

        boolean isTargetOnline = onlineOfflineService.isUserOnline(friend.getId());
        if (isTargetOnline) {
            ChatMessage.ChatMessageBuilder chatMessageBuilder = ChatMessage.builder();
            ChatMessage chatMessage = chatMessageBuilder
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

    public void acceptFriendRequest(UUID senderId) {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), user.getId());

        if (friendRequest == null) {
            throw new EntityException("No friend request found between these users.");
        }

        if (userService.areAlreadyFriends(user.getId(), sender.getId())) {
            friendRequestRepository.delete(friendRequest);
            throw new EntityException("This user is already in your friends list.");
        }

        user.getFriends().add(sender);
        friendRequestRepository.delete(friendRequest);
        userService.addUser(user);
    }

    public void declineFriendRequest(UUID senderId) {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), user.getId());

        if (friendRequest == null) {
            throw new EntityException("No friend request found between these users.");
        }

        friendRequestRepository.delete(friendRequest);
    }

    public void cancelFriendRequest(UUID friendId) {
        UserDetailsImpl userDetails = securityUtils.getUser();
        UUID currentUserId = userDetails.getId();

        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new EntityException("The current user doesn't exist."));
        User friend = userService.getUserById(friendId)
                .orElseThrow(() -> new EntityException("The requested user does not exist."));

        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(friend.getId(), user.getId());

        if (friendRequest == null) {
            throw new EntityException("No friend request found between these users.");
        }

        friendRequestRepository.delete(friendRequest);
    }

}
