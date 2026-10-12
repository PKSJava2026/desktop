package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.UserApi;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.ForwarderRequest;
import java.util.List;

final class MockUserApi implements UserApi {

    private final MockStore store;
    private final MockSession session;

    MockUserApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public UserDto me() {
        return session.current();
    }

    @Override
    public List<UserDto> list(Role role) {
        session.requireStaff();
        return store.users.values().stream()
                .filter(UserDto::active)
                .filter(user -> role == null || user.role() == role)
                .toList();
    }

    @Override
    public List<UserDto> forwarders() {
        session.require(Role.ADMIN);
        return store.users.values().stream()
                .filter(user -> user.role() == Role.FORWARDER && user.active())
                .toList();
    }

    @Override
    public UserDto createForwarder(ForwarderRequest request) {
        session.require(Role.ADMIN);
        if (request == null) {
            throw MockErrors.invalid("Заполните данные экспедитора");
        }
        return store.registerUser(request.nickname(), request.email(), request.password(), Role.FORWARDER);
    }

    @Override
    public void removeForwarder(long id) {
        session.require(Role.ADMIN);
        UserDto forwarder = store.users.get(id);
        if (forwarder == null) {
            throw MockErrors.notFound("экспедитор", id);
        }
        if (forwarder.role() != Role.FORWARDER) {
            throw MockErrors.conflict("Пользователь #" + id + " не является экспедитором");
        }
        if (!forwarder.active()) {
            throw MockErrors.conflict("Экспедитор #" + id + " уже удалён");
        }
        store.users.put(id, MockCopies.deactivated(forwarder));
    }
}
