package fr.iban.network.mscore;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.teleport.SLocation;
import fr.iban.network.NetworkLocation;
import fr.iban.network.NetworkTeleporter;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

class MSCoreTeleporter implements NetworkTeleporter {

    private final CoreBukkitPlugin core;

    MSCoreTeleporter(CoreBukkitPlugin core) {
        this.core = core;
    }

    /**
     * The teleport is routed through the proxy, so the result only says the request was handed to MSCore.
     */
    @Override
    public CompletableFuture<Boolean> teleport(Player player, NetworkLocation location, int delaySeconds) {
        core.getTeleportManager().teleport(player, toSLocation(location), delaySeconds);
        return CompletableFuture.completedFuture(true);
    }

    static SLocation toSLocation(NetworkLocation location) {
        return new SLocation(location.server(), location.world(),
                location.x(), location.y(), location.z(), location.pitch(), location.yaw());
    }
}
