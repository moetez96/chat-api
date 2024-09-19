package com.example.chatapi.controller;

import com.example.chatapi.model.UserConnection;
import com.example.chatapi.service.IContactService;
import com.example.chatapi.service.impl.ContactService;
import com.example.chatapi.service.impl.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping(("/api/contact"))
public class ContactController {
    private final IContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/getAllContacts")
    public List<UserConnection> getAllContacts() {
        System.out.println("hello");
        return contactService.getAllContacts();
    }

    @GetMapping("/friends")
    public List<UserConnection> getUserFriends() {
        return contactService.getUserFriends();
    }

    @GetMapping("/addFriend/{userId}")
    public List<UserConnection> addFriend(@PathVariable("userId") String userId) {
        return contactService.addFriend(userId);
    }
}
