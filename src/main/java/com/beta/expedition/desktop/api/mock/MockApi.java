package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.api.AuthApi;
import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.api.CooperationApi;
import com.beta.expedition.desktop.api.NotificationApi;
import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.api.RatingApi;
import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.api.UserApi;

public final class MockApi implements Api {

    private static final MockStore STORE = new MockStore();
    private static final MockSession SESSION = new MockSession(STORE);
    private static final MockContractApi CONTRACTS = new MockContractApi(STORE, SESSION);

    static {
        MockSeed.populate(STORE);
    }

    private final AuthApi auth = locked(AuthApi.class, new MockAuthApi(STORE, SESSION));
    private final UserApi users = locked(UserApi.class, new MockUserApi(STORE, SESSION));
    private final OrderApi orders = locked(OrderApi.class, new MockOrderApi(STORE, SESSION));
    private final ContractApi contracts = locked(ContractApi.class, CONTRACTS);
    private final CooperationApi cooperation = locked(CooperationApi.class,
            new MockCooperationApi(STORE, SESSION, CONTRACTS));
    private final NotificationApi notifications = locked(NotificationApi.class,
            new MockNotificationApi(STORE, SESSION));
    private final RatingApi ratings = locked(RatingApi.class, new MockRatingApi(STORE, SESSION));
    private final ReportApi reports = locked(ReportApi.class, new MockReportApi(STORE, SESSION));

    public MockApi() {
    }

    private static <T> T locked(Class<T> type, T target) {
        return MockLocking.synchronize(type, target, STORE);
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
