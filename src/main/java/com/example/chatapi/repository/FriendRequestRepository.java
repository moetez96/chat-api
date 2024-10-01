package com.example.chatapi.repository;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findFriendRequestBySenderId(UUID sender_id);
    List<FriendRequest> findFriendRequestByReceiverId(UUID receiver_id);

    @Query(value = "SELECT * FROM friend_requests WHERE receiver = :receiver_id AND delivery_status != 'SEEN' ORDER BY created_at DESC", nativeQuery = true)
    List<FriendRequest> findFriendRequestUnseen(UUID receiver_id);
    FriendRequest findBySenderIdAndReceiverId(UUID sender_id, UUID receiver_id);
    boolean existsBySenderIdAndReceiverId(UUID sender_id, UUID receiver_id);

    FriendRequest findFriendRequestBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
}
