package com.example.chatapi.config;

import com.example.chatapi.entity.Role;
import com.example.chatapi.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.example.chatapi.entity.Role.*;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        List<ERole> roles = Arrays.asList(ERole.ADMIN, ERole.MODERATOR, ERole.USER);

        roles.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setId(UUID.randomUUID());
                role.setName(roleName);
                roleRepository.save(role);
            }
        });

        log.info("Roles initialized");
    }
}