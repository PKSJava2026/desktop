package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.NotificationApi;
import com.beta.expedition.desktop.model.NotificationDto;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.List;

public class NotificationService {

    private final NotificationApi api;
    private final IntegerProperty unread = new SimpleIntegerProperty();

    public NotificationService(NotificationApi api) {
        this.api = api;
    }

    public IntegerProperty unreadProperty() {
        return unread;
    }

    public List<NotificationDto> list() {
        return api.list();
    }

    public int unreadCount() {
        return api.unreadCount();
    }

    public void markRead(long id) {
        api.markRead(id);
    }

    public int markAllRead() {
        return api.markAllRead();
    }

    public static int countUnread(List<NotificationDto> notifications) {
        return (int) notifications.stream().filter(notification -> !notification.read()).count();
    }
}
