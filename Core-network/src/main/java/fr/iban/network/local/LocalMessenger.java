package fr.iban.network.local;

import fr.iban.network.NetworkMessage;
import fr.iban.network.NetworkMessenger;
import fr.iban.network.Subscription;

import java.util.function.Consumer;

/**
 * Messages are only delivered to other servers, and there are none.
 */
class LocalMessenger implements NetworkMessenger {

    @Override
    public void publish(String channel, String payload) {
    }

    @Override
    public void publish(String channel, Object payload) {
    }

    @Override
    public Subscription subscribe(String channel, Consumer<NetworkMessage> handler) {
        return () -> {
        };
    }
}
