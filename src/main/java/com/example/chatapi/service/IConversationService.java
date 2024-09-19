package com.example.chatapi.service;

import com.example.chatapi.model.*;

import java.util.*;

public interface IConversationService {

    List<UnseenMessageCountResponse> getUnseenMessageCount();

    ChatMessage getLastMessage(String convId);

    List<ChatMessage> setReadMessages(List<ChatMessage> chatMessages);

    List<ChatMessage> getConversationMessages(String convId);

}
