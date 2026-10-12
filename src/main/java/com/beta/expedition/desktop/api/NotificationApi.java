package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.NotificationDto;
import java.util.List;

public interface NotificationApi {

    List<NotificationDto> list();

    int unreadCount();

    void markRead(long id);

    int markAllRead();
}
