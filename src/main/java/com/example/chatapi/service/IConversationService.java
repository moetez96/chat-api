package com.example.chatapi.service;

import com.example.chatapi.model.*;

import java.util.*;

public interface IConversationService {

    List<UserConnection> getUserFriends();

    List<UnseenMessageCountResponse> getUnseenMessageCount();

    List<ChatMessage> getUnseenMessages(UUID fromUserId);

    List<ChatMessage> setReadMessages(List<ChatMessage> chatMessages);

    List<ChatMessage> getConversationMessages(String convId);

}
