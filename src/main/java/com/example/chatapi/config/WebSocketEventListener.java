package com.example.chatapi.config;

import com.example.chatapi.service.IOnlineOfflineService;
import com.example.chatapi.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketEventListener {

    private final IOnlineOfflineService onlineOfflineService;

    private final SecurityUtils securityUtils;

    private final Map<String, String> simpSessionIdToSubscriptionId;

    private final Map<UUID, Set<String>> activeUserSessions;

    public WebSocketEventListener(IOnlineOfflineService onlineOfflineService, SecurityUtils securityUtils) {
        this.onlineOfflineService = onlineOfflineService;
        this.securityUtils = securityUtils;

        this.simpSessionIdToSubscriptionId = new ConcurrentHashMap<>();
        this.activeUserSessions = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String simpSessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        if (simpSessionId == null || event.getUser() == null) {
            return;
        }

        UUID userId = securityUtils.getUserDetails(event.getUser()).getId();
        Set<String> userSessions = this.activeUserSessions.get(userId);

        if (userSessions != null) {
            userSessions.remove(simpSessionId);

            if (userSessions.isEmpty()) {
                this.activeUserSessions.remove(userId);
                onlineOfflineService.removeOnlineUser(event.getUser());
            }
        }
    }


    @EventListener
    @SendToUser
    public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
        String subscribedChannel =
                (String) sessionSubscribeEvent.getMessage().getHeaders().get("simpDestination");
        String simpSessionId =
                (String) sessionSubscribeEvent.getMessage().getHeaders().get("simpSessionId");
        if (subscribedChannel == null) {
            log.error("SUBSCRIBED TO NULL");
            return;
        }
        simpSessionIdToSubscriptionId.put(simpSessionId, subscribedChannel);
        onlineOfflineService.addUserSubscribed(sessionSubscribeEvent.getUser(), subscribedChannel);
    }

    @EventListener
    public void handleUnSubscribeEvent(SessionUnsubscribeEvent unsubscribeEvent) {
        String simpSessionId = (String) unsubscribeEvent.getMessage().getHeaders().get("simpSessionId");
        String unSubscribedChannel = simpSessionIdToSubscriptionId.get(simpSessionId);
        onlineOfflineService.removeUserSubscribed(unsubscribeEvent.getUser(), unSubscribedChannel);
    }

    @EventListener
    public void handleConnectedEvent(SessionConnectedEvent sessionConnectedEvent) {
        String simpSessionId = (String) sessionConnectedEvent.getMessage().getHeaders().get("simpSessionId");

        if (simpSessionId == null || sessionConnectedEvent.getUser() == null) {
            return;
        }

        UUID userId = securityUtils.getUserDetails(sessionConnectedEvent.getUser()).getId();

        Set<String> userSessions = this.activeUserSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        userSessions.add(simpSessionId);

        if (userSessions.size() == 1) {
            onlineOfflineService.addOnlineUser(sessionConnectedEvent.getUser());
        }
    }
}
