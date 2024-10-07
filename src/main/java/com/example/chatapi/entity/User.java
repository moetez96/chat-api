package com.example.chatapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "username", length = 100)
    private String username;

    @Column
    private String email;

    @Column
    private String password;

    @Column(name = "color_avatar")
    private String colorAvatar;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private Set<User> friends = new HashSet<>();

    @PrePersist
    public void generateRandomColor() {
        if (this.colorAvatar == null) {
            this.colorAvatar = generateRandomHexColor();
        }
    }

    private String generateRandomHexColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
