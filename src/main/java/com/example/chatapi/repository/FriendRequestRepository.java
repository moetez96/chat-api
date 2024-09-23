package com.example.chatapi.repository;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findFriendRequestBySenderId(UUID sender_id);
    List<FriendRequest> findFriendRequestByReceiverId(UUID receiver_id);
    FriendRequest findBySenderIdAndReceiverId(UUID sender_id, UUID receiver_id);
    boolean existsBySenderIdAndReceiverId(UUID sender_id, UUID receiver_id);
}
