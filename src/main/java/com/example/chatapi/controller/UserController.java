package com.example.chatapi.controller;

import com.example.chatapi.dto.ApiResponse;
import com.example.chatapi.model.UserResponse;
import com.example.chatapi.service.impl.OnlineOfflineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final OnlineOfflineService onlineOfflineService;

    public UserController(OnlineOfflineService onlineOfflineService) {
        this.onlineOfflineService = onlineOfflineService;
    }

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getOnlineUsers() {
        try {
            List<UserResponse> onlineUsers = onlineOfflineService.getOnlineUsers();
            return ResponseEntity.ok(new ApiResponse<>("success", "Fetched online users successfully", onlineUsers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Map<String, Set<String>>>> getSubscriptions() {
        try {
            Map<String, Set<String>> subscribedUsers =  onlineOfflineService.getUserSubscribed();
            return ResponseEntity.ok(new ApiResponse<>("success", "Fetched subscribed users successfully", subscribedUsers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }
}
