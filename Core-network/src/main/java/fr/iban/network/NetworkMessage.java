package fr.iban.network;

import com.google.gson.Gson;

public record NetworkMessage(String channel, String sourceServer, String payload) {

    private static final Gson GSON = new Gson();

    public <T> T payloadAs(Class<T> type) {
        return GSON.fromJson(payload, type);
    }
}
