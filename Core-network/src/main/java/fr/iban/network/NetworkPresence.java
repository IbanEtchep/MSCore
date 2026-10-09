package fr.iban.network;

import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Players known to the network and where they are connected.
 */
public interface NetworkPresence {

    /**
     * @return true if the player is connected to any server of the network
     */
    boolean isOnline(UUID uniqueId);

    Set<String> onlinePlayerNames();

    /**
     * Looks up a player who has already joined the network, online or not.
     */
    Optional<NetworkPlayer> findPlayer(UUID uniqueId);

    /**
     * Looks up a player who has already joined the network, online or not.
     */
    Optional<NetworkPlayer> findPlayer(String name);

    /**
     * Sends a message to the player on whichever server they are connected to.
     * Does nothing if the player is offline.
     */
    void sendMessage(UUID uniqueId, Component message);
}
