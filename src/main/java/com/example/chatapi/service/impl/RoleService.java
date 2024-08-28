package com.example.chatapi.service.impl;

import com.example.chatapi.entity.Role;
import com.example.chatapi.repository.RoleRepository;
import com.example.chatapi.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Optional<Role> getByName(Role.ERole name) {
        return roleRepository.findByName(name);
    }
}
