package com.example.chatapi.service;

import com.example.chatapi.entity.User;

import java.util.*;

public interface IUserService {

    void addUser(User user);
    Optional<User> getUserById(UUID uuid);
    Optional<User> getUserByUsername(String username);
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);

    List<User> getAllByUsernames(Set<String> usernames);

    List<User> getAllByIds(List<UUID> fromUsersIds);
}
