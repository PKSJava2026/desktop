package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.UserApi;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.ForwarderRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

final class HttpUserApi implements UserApi {

    private static final TypeReference<List<UserDto>> USERS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpUserApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public UserDto me() {
        return transport.get("/api/users/me", UserDto.class);
    }

    @Override
    public List<UserDto> list(Role role) {
        return transport.get("/api/users", new Query().add("role", role), USERS);
    }

    @Override
    public List<UserDto> forwarders() {
        return transport.get("/api/forwarders", USERS);
    }

    @Override
    public UserDto createForwarder(ForwarderRequest request) {
        return transport.post("/api/forwarders", request, UserDto.class);
    }

    @Override
    public void removeForwarder(long id) {
        transport.delete("/api/forwarders/" + id);
    }
}
