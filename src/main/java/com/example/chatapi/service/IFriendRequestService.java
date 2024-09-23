package com.example.chatapi.service;

import com.example.chatapi.entity.FriendRequest;
import com.example.chatapi.entity.User;
import com.example.chatapi.model.FriendRequestResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IFriendRequestService {

    List<FriendRequestResponse> getFriendsRequests();

    FriendRequestResponse addFriendRequest(UUID friendId);

    void acceptFriendRequest(UUID friendId);

    void declineFriendRequest(UUID friendId);

    void cancelFriendRequest(UUID friendId);

}
