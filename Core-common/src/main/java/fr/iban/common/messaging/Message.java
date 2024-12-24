package fr.iban.common.messaging;

import fr.iban.common.utils.GsonProvider;

public class Message {

    private String channel;
    private String serverFrom;
    private String message;

    public Message(String channel, String serverFrom, String message) {
        this.channel = channel;
        this.serverFrom = serverFrom;
        this.message = message;
    }

    public Message() {}

    public String getChannel() {
        return channel;
    }

    public String getServerFrom() {
        return serverFrom;
    }

    public String getMessage() {
        return message;
    }

    public <T> T getMessage(Class<T> clazz) {
        return GsonProvider.getGson().fromJson(message, clazz);
    }
}
