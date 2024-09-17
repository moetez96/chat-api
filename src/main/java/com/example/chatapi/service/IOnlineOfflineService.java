package com.example.chatapi.service;

import com.example.chatapi.entity.Conversation;
import com.example.chatapi.model.*;

import java.security.Principal;
import java.util.*;

public interface IOnlineOfflineService {

    void addOnlineUser(Principal user);

    void removeOnlineUser(Principal user);

    boolean isUserOnline(UUID userId);

    List<UserResponse> getOnlineUsers();

    void addUserSubscribed(Principal user, String subscribedChannel);

    void removeUserSubscribed(Principal user, String subscribedChannel);

     boolean isUserSubscribed(UUID username, String subscription);

    Map<String, Set<String>> getUserSubscribed();

    void notifySender(UUID senderId, List<Conversation> entities,
                      MessageDeliveryStatusEnum messageDeliveryStatusEnum);

    void notifyUsers(String convId, List<Conversation> entities,
                      MessageDeliveryStatusEnum messageDeliveryStatusEnum);
}
