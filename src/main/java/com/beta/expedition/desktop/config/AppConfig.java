package com.beta.expedition.desktop.config;

import com.beta.expedition.desktop.api.ApiException;
import com.beta.expedition.desktop.api.ApiMode;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppConfig {

    private static final String RESOURCE = "/app.properties";
    private static final String API_MODE = "api.mode";
    private static final String API_BASE_URL = "api.baseUrl";

    private final Properties properties;

    private AppConfig(Properties properties) {
        this.properties = properties;
    }

    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new ApiException("Не найден файл настроек " + RESOURCE);
            }
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ApiException("Не удалось прочитать файл настроек", e);
        }
        return new AppConfig(properties);
    }

    public ApiMode apiMode() {
        String value = properties.getProperty(API_MODE, "mock").trim().toUpperCase();
        try {
            return ApiMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Неизвестный режим API в настройках: " + value, e);
        }
    }

    public String apiBaseUrl() {
        return properties.getProperty(API_BASE_URL, "http://localhost:8080").trim();
    }
}
