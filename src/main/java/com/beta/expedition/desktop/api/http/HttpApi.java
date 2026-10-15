package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.api.AuthApi;
import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.api.CooperationApi;
import com.beta.expedition.desktop.api.NotificationApi;
import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.api.RatingApi;
import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.api.UserApi;
import com.beta.expedition.desktop.api.mock.MockApi;
import com.beta.expedition.desktop.config.AppConfig;
import com.beta.expedition.desktop.model.AuthResult;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.LoginRequest;
import com.beta.expedition.desktop.model.request.RegisterRequest;

public final class HttpApi implements Api {

    private final HttpAuthApi remoteAuth;
    private final MockApi local = new MockApi();
    private final AuthApi auth;

    public HttpApi() {
        this.remoteAuth = new HttpAuthApi(new HttpTransport(AppConfig.load().apiBaseUrl()));
        this.auth = new AuthApi() {
            @Override
            public UserDto register(RegisterRequest request) {
                return remoteAuth.register(request);
            }

            @Override
            public AuthResult login(LoginRequest request) {
                AuthResult remote = remoteAuth.login(request);
                UserDto adopted = local.adopt(remote.user());
                UserDto user = new UserDto(adopted.id(), remote.user().nickname(), remote.user().email(),
                        remote.user().role(), remote.user().active(), remote.user().createdAt());
                return new AuthResult(remote.token(), user);
            }

            @Override
            public void logout() {
                remoteAuth.logout();
                local.release();
            }
        };
    }

    @Override
    public AuthApi auth() {
        return auth;
    }

    @Override
    public UserApi users() {
        return local.users();
    }

    @Override
    public OrderApi orders() {
        return local.orders();
    }

    @Override
    public ContractApi contracts() {
        return local.contracts();
    }

    @Override
    public CooperationApi cooperation() {
        return local.cooperation();
    }

    @Override
    public NotificationApi notifications() {
        return local.notifications();
    }

    @Override
    public RatingApi ratings() {
        return local.ratings();
    }

    @Override
    public ReportApi reports() {
        return local.reports();
    }
}
