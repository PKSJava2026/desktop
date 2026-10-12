package com.beta.expedition.desktop.api;

public interface Api {

    AuthApi auth();

    UserApi users();

    OrderApi orders();

    ContractApi contracts();

    CooperationApi cooperation();

    NotificationApi notifications();

    RatingApi ratings();

    ReportApi reports();
}
