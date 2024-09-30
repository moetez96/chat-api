package com.example.chatapi.entity;

import com.example.chatapi.model.MessageDeliveryStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "friend_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender", columnDefinition = "uuid", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver", columnDefinition = "uuid", nullable = false)
    private User receiver;

    @Column(name = "created_at")
    @CreatedDate
    private Timestamp createdAt;

    @Column(name = "delivery_status", length = -1)
    private String deliveryStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = Timestamp.from(Instant.now());
    }
}
