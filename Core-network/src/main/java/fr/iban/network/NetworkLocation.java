package fr.iban.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

/**
 * A location on a given server of the network.
 * <p>
 * Field names match MSCore's {@code SLocation}, so JSON written by Gson for one reads as the other.
 */
public record NetworkLocation(String server, String world, double x, double y, double z, float pitch, float yaw) {

    public static NetworkLocation of(String server, Location location) {
        return new NetworkLocation(server, location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    /**
     * @return the Bukkit location on this server, empty if the world is not loaded here
     */
    public Optional<Location> toBukkit() {
        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            return Optional.empty();
        }
        return Optional.of(new Location(bukkitWorld, x, y, z, yaw, pitch));
    }
}
