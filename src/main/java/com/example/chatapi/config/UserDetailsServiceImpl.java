package com.example.chatapi.config;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.chatapi.entity.User;
import com.example.chatapi.service.IUserService;
import com.example.chatapi.service.impl.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final IUserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =
                userService
                        .getUserByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " not found"));

        Set<GrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .collect(Collectors.toSet());

        return UserDetailsImpl.builder()
                .username(user.getUsername())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .password(user.getPassword())
                .authorities(authorities)
                .id(user.getId())
                .build();
    }
}
