package com.example.chatapi.controller;

import com.example.chatapi.dto.ApiResponse;
import com.example.chatapi.dto.FriendRequestResponse;
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

    @GetMapping("/getSentRequests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getSentRequests() {
        try {
            List<FriendRequestResponse> sentRequests = friendRequestService.getSentRequests();
            return ResponseEntity.ok(new ApiResponse<>("success", "Sent requests fetched successfully", sentRequests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/getReceivedRequests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getReceivedRequests() {
        try {
            List<FriendRequestResponse> receivedRequests = friendRequestService.getReceivedRequests();
            return ResponseEntity.ok(new ApiResponse<>("success", "Received requests fetched successfully", receivedRequests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/getReceivedUnseenRequests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getReceivedUnseenRequests() {
        try {
            List<FriendRequestResponse> sentRequests = friendRequestService.getReceivedUnseenRequests();
            return ResponseEntity.ok(new ApiResponse<>("success", "Sent requests fetched successfully", sentRequests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/seeFriendsRequests")
    public ResponseEntity<ApiResponse<?>> seeFriendsRequests() {
        try {
            friendRequestService.seeFriendsRequests();
            return ResponseEntity.ok(new ApiResponse<>("success", "Requests seen successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/addFriendRequest/{friendId}")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> addFriendRequest(@PathVariable String friendId) {
        try {
            FriendRequestResponse response = friendRequestService.addFriendRequest(UUID.fromString(friendId));
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("success", "Friend request sent", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/acceptFriendRequest/{senderId}")
    public ResponseEntity<ApiResponse<?>> acceptFriendRequest(@PathVariable String senderId) {
        try {
            friendRequestService.acceptFriendRequest(UUID.fromString(senderId));
            return ResponseEntity.ok(new ApiResponse<>("success", "Request accepted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/declineFriendRequest/{senderId}")
    public ResponseEntity<ApiResponse<?>> declineFriendRequest(@PathVariable String senderId) {
        try {
            friendRequestService.declineFriendRequest(UUID.fromString(senderId));
            return ResponseEntity.ok(new ApiResponse<>("success", "Request declined successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @DeleteMapping("/cancelFriendRequest/{friendId}")
    public ResponseEntity<ApiResponse<?>> cancelFriendRequest(@PathVariable String friendId) {
        try {
            friendRequestService.cancelFriendRequest(UUID.fromString(friendId));
            return ResponseEntity.ok(new ApiResponse<>("success", "Request canceled successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }
}
