package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

final class HttpTransport {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final String baseUrl;
    private volatile String token;

    HttpTransport(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    void setToken(String token) {
        this.token = token;
    }

    void clearToken() {
        this.token = null;
    }

    <T> T get(String path, Class<T> type) {
        return send("GET", path, null, mapper.constructType(type));
    }

    <T> T get(String path, TypeReference<T> type) {
        return send("GET", path, null, mapper.getTypeFactory().constructType(type));
    }

    <T> T get(String path, Query query, TypeReference<T> type) {
        return get(path + query, type);
    }

    <T> T post(String path, Object body, Class<T> type) {
        return send("POST", path, body, mapper.constructType(type));
    }

    <T> T post(String path, Object body, TypeReference<T> type) {
        return send("POST", path, body, mapper.getTypeFactory().constructType(type));
    }

    byte[] getBytes(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "*/*");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpResponse<byte[]> response = execute(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() / 100 != 2) {
            throw HttpErrors.from(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8), mapper);
        }
        return response.body();
    }

    <T> T put(String path, Object body, Class<T> type) {
        return send("PUT", path, body, mapper.constructType(type));
    }

    void delete(String path) {
        send("DELETE", path, null, mapper.constructType(Void.class));
    }

    private <T> T send(String method, String path, Object body, JavaType type) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(toJson(body), StandardCharsets.UTF_8));
        }
        HttpResponse<String> response = execute(builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 100 != 2) {
            throw HttpErrors.from(response.statusCode(), response.body(), mapper);
        }
        String text = response.body();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(text, type);
        } catch (JsonProcessingException e) {
            throw new ApiException("Сервер вернул данные в неожиданном формате", e);
        }
    }

    private <B> HttpResponse<B> execute(HttpRequest request, HttpResponse.BodyHandler<B> handler) {
        try {
            return client.send(request, handler);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запрос к серверу был прерван", e);
        } catch (IOException e) {
            throw new ApiException("Не удалось подключиться к серверу " + baseUrl
                    + ". Проверьте, что сервис запущен и адрес указан верно.", e);
        }
    }

    private String toJson(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ApiException("Не удалось подготовить данные для отправки", e);
        }
    }
}
