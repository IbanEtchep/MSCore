package fr.iban.network;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface NetworkTeleporter {

    /**
     * Teleports the player to a location, possibly on another server.
     *
     * @param delaySeconds delay before the teleport, 0 for immediate
     * @return completes with false if the teleport was refused or failed. For a cross-server
     * teleport it completes with true once the request is sent, since the outcome happens on another server.
     */
    CompletableFuture<Boolean> teleport(Player player, NetworkLocation location, int delaySeconds);

    default CompletableFuture<Boolean> teleport(Player player, NetworkLocation location) {
        return teleport(player, location, 0);
    }
}
