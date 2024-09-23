package com.example.chatapi.controller;

import com.example.chatapi.model.FriendRequestResponse;
import com.example.chatapi.service.IFriendRequestService;
import com.example.chatapi.service.impl.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(("/api/request"))
public class FriendRequestController {

    private final IFriendRequestService friendRequestService;

    @Autowired
    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @GetMapping("/getFriendsRequests")
    public List<FriendRequestResponse> getFriendsRequests() {
        return friendRequestService.getFriendsRequests();
    }

    @PostMapping("/addFriendRequest/{friendId}")
    public FriendRequestResponse addFriendRequest(@PathVariable String friendId) {
        return friendRequestService.addFriendRequest(UUID.fromString(friendId));
    }

    @PutMapping("/acceptFriendRequest/{senderId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String senderId) {
        try {
            friendRequestService.acceptFriendRequest(UUID.fromString(senderId));
            return ResponseEntity.ok("Request accepted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/declineFriendRequest/{senderId}")
    public ResponseEntity<?> declineFriendRequest(@PathVariable String senderId) {
        try {
            friendRequestService.declineFriendRequest(UUID.fromString(senderId));
            return ResponseEntity.ok("Request declined successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/cancelFriendRequest/{friendId}")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable String friendId) {
        try {
            friendRequestService.cancelFriendRequest(UUID.fromString(friendId));
            return ResponseEntity.ok("Request canceled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
