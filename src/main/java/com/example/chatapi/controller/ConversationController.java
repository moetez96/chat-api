package com.example.chatapi.controller;

import com.example.chatapi.dto.ApiResponse;
import com.example.chatapi.model.ChatMessage;
import com.example.chatapi.model.UnseenMessageCountResponse;
import com.example.chatapi.model.UserConnection;
import com.example.chatapi.service.impl.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(("/api/conversation"))
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/unseenMessages")
    public ResponseEntity<ApiResponse<List<UnseenMessageCountResponse>>> getUnseenMessageCount() {

        try {
            List<UnseenMessageCountResponse> unseenMessages = conversationService.getUnseenMessageCount();
            return ResponseEntity.ok(new ApiResponse<>("success", "Unseen messages fetched successfully", unseenMessages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/getLastMessage/{convId}")
    public ResponseEntity<ApiResponse<ChatMessage>> getLastMessage(@PathVariable("convId") String convId) {

        try {
            ChatMessage lastMessage = conversationService.getLastMessage(convId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Last message fetched successfully", lastMessage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/setReadMessages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> setReadMessages(@RequestBody List<ChatMessage> chatMessages) {

        try {
            List<ChatMessage> readChatMessages = conversationService.setReadMessages(chatMessages);
            return ResponseEntity.ok(new ApiResponse<>("success", "Read chat messages fetched successfully", readChatMessages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/getConversationMessages/{convId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getConversationMessages(@PathVariable("convId") String convId) {

        try {
            List<ChatMessage> chatMessages = conversationService.getConversationMessages(convId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Chat messages fetched successfully", chatMessages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }
}
