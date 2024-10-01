package com.example.chatapi.service;

import com.example.chatapi.model.UserConnection;

import java.util.List;
import java.util.UUID;

public interface IContactService {

    List<UserConnection> getAllContacts();

    List<UserConnection> getUserFriends();

    UserConnection getFriendById(UUID friendId);
}
