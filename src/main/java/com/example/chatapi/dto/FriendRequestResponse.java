package com.example.chatapi.dto;

import com.example.chatapi.model.UserConnection;
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
