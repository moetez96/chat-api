package com.example.chatapi.service;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.model.ChatMessage;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public interface IChatService {
    void sendMessageToConvId(ChatMessage chatMessage, String conversationId, SimpMessageHeaderAccessor headerAccessor);
    UserDetailsImpl getUser();
}
