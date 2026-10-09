package fr.iban.network;

/**
 * Entry point to the cross-server services used by MS plugins.
 * <p>
 * Obtain one with {@link NetworkBridges#create(org.bukkit.plugin.Plugin)} in {@code onEnable}
 * and {@link #close()} it in {@code onDisable}.
 */
public interface NetworkBridge extends AutoCloseable {

    /**
     * @return name of this server on the network, used as {@link NetworkLocation#server()}
     */
    String serverName();

    NetworkMessenger messenger();

    NetworkPresence presence();

    NetworkTeleporter teleporter();

    /**
     * Unregisters every subscription made through this bridge.
     */
    @Override
    void close();
}
