package com.example.chatapi.service;

import com.example.chatapi.entity.Role;

import java.util.Optional;

public interface IRoleService {

    Optional<Role> getByName(Role.ERole name);
}
