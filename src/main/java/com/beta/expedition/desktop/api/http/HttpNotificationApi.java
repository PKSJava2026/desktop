package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.NotificationApi;
import com.beta.expedition.desktop.model.NotificationDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

final class HttpNotificationApi implements NotificationApi {

    private static final TypeReference<List<NotificationDto>> NOTIFICATIONS = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Integer>> COUNTS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpNotificationApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public List<NotificationDto> list() {
        return transport.get("/api/notifications", NOTIFICATIONS);
    }

    @Override
    public int unreadCount() {
        return transport.get("/api/notifications/unread-count", COUNTS).getOrDefault("count", 0);
    }

    @Override
    public void markRead(long id) {
        transport.post("/api/notifications/" + id + "/read", null, Void.class);
    }

    @Override
    public int markAllRead() {
        return transport.post("/api/notifications/read-all", null, COUNTS).getOrDefault("updated", 0);
    }
}
