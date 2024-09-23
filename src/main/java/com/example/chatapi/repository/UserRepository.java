package com.example.chatapi.repository;

import com.example.chatapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findAllByUsernameIn(Set<String> usernames);

    @Query("select case when count(uf) > 0 then true else false end " +
            "from User u join u.friends uf " +
            "where u.id = :userId and uf.id = :friendId")
    boolean areUsersFriends(@Param("userId") UUID userId, @Param("friendId") UUID friendId);
}
