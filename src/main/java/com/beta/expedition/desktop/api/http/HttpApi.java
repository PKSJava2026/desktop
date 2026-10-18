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
    private final UserApi users;
    private final OrderApi orders;
    private final ContractApi contracts;
    private final CooperationApi cooperation;
    private final NotificationApi notifications;
    private final RatingApi ratings;
    private final ReportApi reports;
    private final MockApi local = new MockApi();
    private final AuthApi auth;

    public HttpApi() {
        HttpTransport transport = new HttpTransport(AppConfig.load().apiBaseUrl());
        this.remoteAuth = new HttpAuthApi(transport);
        this.users = new HttpUserApi(transport);
        this.orders = new HttpOrderApi(transport);
        this.contracts = new HttpContractApi(transport);
        this.cooperation = new HttpCooperationApi(transport);
        this.notifications = new HttpNotificationApi(transport);
        this.ratings = new HttpRatingApi(transport);
        this.reports = new HttpReportApi(transport);
        this.auth = new AuthApi() {
            @Override
            public UserDto register(RegisterRequest request) {
                return remoteAuth.register(request);
            }

            @Override
            public AuthResult login(LoginRequest request) {
                AuthResult remote = remoteAuth.login(request);
                local.adopt(remote.user());
                return remote;
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
        return users;
    }

    @Override
    public OrderApi orders() {
        return orders;
    }

    @Override
    public ContractApi contracts() {
        return contracts;
    }

    @Override
    public CooperationApi cooperation() {
        return cooperation;
    }

    @Override
    public NotificationApi notifications() {
        return notifications;
    }

    @Override
    public RatingApi ratings() {
        return ratings;
    }

    @Override
    public ReportApi reports() {
        return reports;
    }
}
