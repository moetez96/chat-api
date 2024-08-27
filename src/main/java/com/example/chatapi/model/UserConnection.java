package com.example.chatapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserConnection {
    private UUID connectionId;
    private String connectionUsername;
    private String convId;
    private int unSeen;

    @JsonProperty("isOnline")
    private boolean isOnline;
}
