package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.ForwarderRequest;
import java.util.List;

public interface UserApi {

    UserDto me();

    List<UserDto> list(Role role);

    List<UserDto> forwarders();

    UserDto createForwarder(ForwarderRequest request);

    void removeForwarder(long id);
}
