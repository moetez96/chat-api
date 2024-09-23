package com.example.chatapi.service;

import com.example.chatapi.model.UserConnection;

import java.util.List;

public interface IContactService {

    List<UserConnection> getAllContacts();

    List<UserConnection> getUserFriends();

}
