package fr.iban.common.messaging;

import fr.iban.common.utils.GsonProvider;

public abstract class AbstractMessagingManager {

    protected AbstractMessenger messenger;

    /**
     * Init an implementation of AbstractMessenger
     */
    public abstract void init();

    public abstract void close();

    protected abstract String getServerName();

    public void sendMessage(String channel, String jsonMsg) {
        messenger.sendMessage(new Message(channel, getServerName(), jsonMsg));
    }

    public <T> void sendMessage(String channel, T message) {
        sendMessage(channel, GsonProvider.getGson().toJson(message));
    }

    public AbstractMessenger getMessenger() {
        return messenger;
    }
}
