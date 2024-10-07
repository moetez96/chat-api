package com.example.chatapi.service.impl;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.User;
import com.example.chatapi.exception.EntityException;
import com.example.chatapi.model.UserConnection;
import com.example.chatapi.repository.UserRepository;
import com.example.chatapi.service.IContactService;
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
public class ContactService implements IContactService {

    private final IUserService userService;
    private final SecurityUtils securityUtils;
    private final IOnlineOfflineService onlineOfflineService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @Autowired
    public ContactService(UserService userService,
                          SecurityUtils securityUtils,
                          OnlineOfflineService onlineOfflineService,
                          SimpMessageSendingOperations simpMessageSendingOperations) {
        this.userService = userService;
        this.securityUtils = securityUtils;
        this.onlineOfflineService = onlineOfflineService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    public List<UserConnection> getAllContacts() {
        UserDetailsImpl userDetails = securityUtils.getUser();
        String username = userDetails.getUsername();
        List<User> users = userService.getAll();
        User thisUser =
                users.stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst()
                        .orElseThrow(EntityException::new);

        return users.stream()
                .filter(user -> !user.getUsername().equals(username))
                .map(user ->
                        UserConnection.builder()
                                .connectionId(user.getId())
                                .connectionUsername(user.getUsername())
                                .convId(getConvId(user, thisUser))
                                .unSeen(0)
                                .isOnline(onlineOfflineService.isUserOnline(user.getId()))
                                .avatar(user.getColorAvatar())
                                .build())
                .toList();
    }

    public List<UserConnection> getUserFriends() {
        UserDetailsImpl userDetails = securityUtils.getUser();
        String userName = userDetails.getUsername();
        User thisUser = userService.getUserByUsername(userName).orElseThrow(EntityException::new);

        return thisUser.getFriends().stream()
                .map(friend ->
                        UserConnection.builder()
                                .connectionId(friend.getId())
                                .connectionUsername(friend.getUsername())
                                .convId(getConvId(friend, thisUser))
                                .unSeen(0)
                                .isOnline(onlineOfflineService.isUserOnline(friend.getId()))
                                .avatar(friend.getColorAvatar())
                                .build())
                .toList();
    }


    public UserConnection getFriendById(UUID friendId) {
        UserDetailsImpl userDetails = securityUtils.getUser();
        String userName = userDetails.getUsername();

        User thisUser = userService.getUserByUsername(userName).orElseThrow(EntityException::new);

        User friend = thisUser.getFriends()
                .stream()
                .filter(f -> f.getId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new EntityException("Friend with id " + friendId + " not found"));

        return UserConnection.builder()
                .connectionId(friend.getId())
                .connectionUsername(friend.getUsername())
                .convId(getConvId(friend, thisUser))
                .unSeen(0)
                .isOnline(onlineOfflineService.isUserOnline(friend.getId()))
                .avatar(friend.getColorAvatar())
                .build();
    }

    public static String getConvId(User user1, User user2) {
        String id1 = user1.getId().toString();
        String id2 = user2.getId().toString();

        return id1.compareTo(id2) > 0 ? id2 + "_" + id1 : id1 + "_" + id2;
    }
}
