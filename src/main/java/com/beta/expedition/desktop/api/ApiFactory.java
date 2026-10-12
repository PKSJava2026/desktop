package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.config.AppConfig;

public final class ApiFactory {

    private ApiFactory() {
    }

    public static Api create() {
        ApiMode mode = AppConfig.load().apiMode();
        try {
            Class<?> type = Class.forName(mode.implementationClass());
            return (Api) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ApiException("Режим API " + mode.name().toLowerCase() + " пока не поддерживается", e);
        }
    }
}
