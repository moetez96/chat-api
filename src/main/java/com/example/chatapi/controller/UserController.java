package com.example.chatapi.controller;

import com.example.chatapi.model.UserResponse;
import com.example.chatapi.service.impl.OnlineOfflineService;
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
    List<UserResponse> getOnlineUsers() {
        return onlineOfflineService.getOnlineUsers();
    }

    @GetMapping("/subscriptions")
    Map<String, Set<String>> getSubscriptions() {
        return onlineOfflineService.getUserSubscribed();
    }
}
