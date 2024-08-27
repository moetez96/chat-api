package com.example.chatapi.controller;

import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.UnseenMessageCountResponse;
import com.example.chatapi.model.UserConnection;
import com.example.chatapi.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping(("/api/conversation"))
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/friends")
    public List<UserConnection> getUserFriends() {
        return conversationService.getUserFriends();
    }

    @GetMapping("/unseenMessages")
    public List<UnseenMessageCountResponse> getUnseenMessageCount() {
        return conversationService.getUnseenMessageCount();
    }

    @GetMapping("/unseenMessages/{fromUserId}")
    public List<ChatMessage> getUnseenMessages(@PathVariable("fromUserId") UUID fromUserId) {
        return conversationService.getUnseenMessages(fromUserId);
    }

    @PutMapping("/setReadMessages")
    public List<ChatMessage> setReadMessages(@RequestBody List<ChatMessage> chatMessages) {
        return conversationService.setReadMessages(chatMessages);
    }
}
