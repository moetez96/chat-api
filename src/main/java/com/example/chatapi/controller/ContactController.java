package com.example.chatapi.controller;

import com.example.chatapi.dto.ApiResponse;
import com.example.chatapi.model.UserConnection;
import com.example.chatapi.service.IContactService;
import com.example.chatapi.service.impl.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(("/api/contact"))
public class ContactController {
    private final IContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/getAllContacts")
    public ResponseEntity<ApiResponse<List<UserConnection>>> getAllContacts() {

        try {
            List<UserConnection> listFriends = contactService.getAllContacts();
            return ResponseEntity.ok(new ApiResponse<>("success", "Contacts list fetched successfully", listFriends));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<UserConnection>>> getUserFriends() {

        try {
            List<UserConnection> listFriends = contactService.getUserFriends();
            return ResponseEntity.ok(new ApiResponse<>("success", "Friends list fetched successfully", listFriends));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/friend/{friendId}")
    public ResponseEntity<ApiResponse<UserConnection>> getFriendById(@PathVariable String friendId) {

        try {
            UserConnection friend = contactService.getFriendById(UUID.fromString(friendId));
            return ResponseEntity.ok(new ApiResponse<>("success", "Friend fetched successfully", friend));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", "Invalid UUID format", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

}
