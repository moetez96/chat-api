package com.example.chatapi.service;

import com.example.chatapi.dto.FriendRequestResponse;

import java.util.List;
import java.util.UUID;

public interface IFriendRequestService {

    List<FriendRequestResponse> getSentRequests();

    List<FriendRequestResponse> getReceivedRequests();


    FriendRequestResponse addFriendRequest(UUID friendId);

    void acceptFriendRequest(UUID friendId);

    void declineFriendRequest(UUID friendId);

    void cancelFriendRequest(UUID friendId);

}
