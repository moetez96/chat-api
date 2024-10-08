package com.example.chatapi.mapper;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.dto.FriendRequestResponse;
import com.example.chatapi.model.UserConnection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FriendRequestResponseMapper {

    public List<FriendRequestResponse> toFriendRequestResponses(List<FriendRequest> friendRequests) {
        return  friendRequests.stream().map(this::toFriendRequestResponse).toList();
    }

    public FriendRequestResponse toFriendRequestResponse(FriendRequest friendRequest) {
        return FriendRequestResponse.builder()
                .id(friendRequest.getId())
                .sender(
                        UserConnection.builder()
                                .connectionId(friendRequest.getSender().getId())
                                .connectionUsername(friendRequest.getSender().getUsername())
                                .avatar(friendRequest.getSender().getColorAvatar()).build()
                )
                .receiver(
                        UserConnection.builder()
                                .connectionId(friendRequest.getReceiver().getId())
                                .connectionUsername(friendRequest.getReceiver().getUsername())
                                .avatar(friendRequest.getSender().getColorAvatar()).build()
                )
                .createdAt(friendRequest.getCreatedAt())
                .build();
    }
}
