package com.example.chatapi.controller;

import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.service.IChatService;
import com.example.chatapi.service.impl.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatController {

    private final IChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/sendMessage/{convId}")
    public ChatMessage sendMessageToConvId(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            @DestinationVariable("convId") String conversationId) {
        chatService.sendMessageToConvId(chatMessage, conversationId, headerAccessor);
        return chatMessage;
    }
}