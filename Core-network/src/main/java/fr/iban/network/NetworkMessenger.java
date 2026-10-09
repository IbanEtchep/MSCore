package fr.iban.network;

import java.util.function.Consumer;

/**
 * Publish/subscribe between the servers of the network.
 * <p>
 * Messages published by a server are never delivered back to that same server.
 */
public interface NetworkMessenger {

    void publish(String channel, String payload);

    /**
     * Publishes {@code payload} serialized as JSON with Gson.
     */
    void publish(String channel, Object payload);

    /**
     * Handlers are called off the main thread.
     */
    Subscription subscribe(String channel, Consumer<NetworkMessage> handler);
}
