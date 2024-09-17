package com.example.chatapi.repository;

import com.example.chatapi.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("select c from Conversation c where c.toUser = :toUser and c.deliveryStatus in ('NOT_DELIVERED', 'DELIVERED') and c.fromUser = :fromUser")
    List<Conversation> findUnseenMessages(
            @Param("toUser") UUID toUser, @Param("fromUser") UUID fromUser);
    @Query(value = "SELECT * FROM Conversation WHERE conv_id = :convId ORDER BY time DESC LIMIT 1", nativeQuery = true)
    Optional<Conversation> findLastMessage(@Param("convId") String convId);

    @Query(value = "select * from conversation where to_user = :toUser and delivery_status in ('NOT_DELIVERED', 'DELIVERED')", nativeQuery = true)
    List<Conversation> findUnseenMessagesCount(@Param("toUser") UUID toUser);

    @Query(value = "select * from conversation where conv_id = :convId", nativeQuery = true)
    List<Conversation> findConversationMessages(@Param("convId") String convId);
}