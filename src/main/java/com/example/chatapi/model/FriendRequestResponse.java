package com.example.chatapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestResponse {

    private Long id;

    private UserConnection sender;

    private UserConnection receiver;

    private Timestamp createdAt;
}
