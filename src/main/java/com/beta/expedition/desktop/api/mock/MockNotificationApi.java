package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.NotificationApi;
import com.beta.expedition.desktop.model.NotificationDto;
import com.beta.expedition.desktop.model.UserDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class MockNotificationApi implements NotificationApi {

    private final MockStore store;
    private final MockSession session;

    MockNotificationApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public List<NotificationDto> list() {
        return userNotifications(session.current()).stream()
                .sorted(Comparator.comparing(NotificationDto::id).reversed())
                .toList();
    }

    @Override
    public int unreadCount() {
        return (int) userNotifications(session.current()).stream().filter(n -> !n.read()).count();
    }

    @Override
    public void markRead(long id) {
        List<NotificationDto> list = userNotifications(session.current());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id() == id) {
                list.set(i, MockCopies.asRead(list.get(i)));
                return;
            }
        }
        throw MockErrors.notFound("уведомление", id);
    }

    @Override
    public int markAllRead() {
        List<NotificationDto> list = userNotifications(session.current());
        int changed = 0;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).read()) {
                list.set(i, MockCopies.asRead(list.get(i)));
                changed++;
            }
        }
        return changed;
    }

    private List<NotificationDto> userNotifications(UserDto user) {
        return store.notifications.computeIfAbsent(user.id(), id -> new ArrayList<>());
    }
}
