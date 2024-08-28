package com.example.chatapi.service.impl;

import com.example.chatapi.entity.User;
import com.example.chatapi.repository.UserRepository;
import com.example.chatapi.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository  = userRepository;
    }

    public void addUser(User user) {
        userRepository.save(user);
    }
    public Optional<User> getUserById(UUID uuid) {
        return userRepository.findById(uuid);
    }
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAllByUsernames(Set<String> usernames) {
        return userRepository.findAllByUsernameIn(usernames);
    }

    public List<User> getAllByIds(List<UUID> usersIds) {
        return userRepository.findAllById(usersIds);
    }
}
