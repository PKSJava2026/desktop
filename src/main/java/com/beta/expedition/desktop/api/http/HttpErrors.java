package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

final class HttpErrors {

    private HttpErrors() {
    }

    static ApiException from(int status, String body, ObjectMapper mapper) {
        String message = readMessage(body, mapper);
        if (message == null || message.isBlank()) {
            message = "Ошибка сервера (код " + status + ")";
        }
        return new ApiException(status, message);
    }

    private static String readMessage(String body, ObjectMapper mapper) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode message = mapper.readTree(body).get("message");
            return message == null || message.isNull() ? null : message.asText();
        } catch (IOException e) {
            return null;
        }
    }
}
