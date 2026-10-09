package fr.iban.network.local;

import fr.iban.network.NetworkLocation;
import fr.iban.network.NetworkTeleporter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Teleports on this server. {@link NetworkLocation#server()} is ignored: there is only one server,
 * so locations recorded on a former network still resolve as long as the world exists here.
 */
class LocalTeleporter implements NetworkTeleporter {

    private static final long TICKS_PER_SECOND = 20L;

    private final Plugin plugin;

    LocalTeleporter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, NetworkLocation location, int delaySeconds) {
        Optional<Location> target = location.toBukkit();
        if (target.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Runnable retired = () -> result.complete(false);
        Runnable teleport = () -> player.teleportAsync(target.get()).whenComplete((success, error) ->
                result.complete(error == null && Boolean.TRUE.equals(success)));

        // Entity scheduler: runs on the player's region thread on Folia, on the main thread on Paper.
        boolean scheduled = delaySeconds <= 0
                ? player.getScheduler().run(plugin, task -> teleport.run(), retired) != null
                : player.getScheduler().runDelayed(plugin, task -> teleport.run(), retired, delaySeconds * TICKS_PER_SECOND) != null;

        if (!scheduled) {
            result.complete(false);
        }
        return result;
    }
}
