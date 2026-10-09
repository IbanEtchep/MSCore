package fr.iban.network.local;

import fr.iban.network.NetworkBridge;
import fr.iban.network.NetworkMessenger;
import fr.iban.network.NetworkPresence;
import fr.iban.network.NetworkTeleporter;
import org.bukkit.plugin.Plugin;

/**
 * Single server network: there is no other server to talk to.
 */
public class LocalNetworkBridge implements NetworkBridge {

    public static final String SERVER_NAME = "local";

    private final NetworkMessenger messenger = new LocalMessenger();
    private final NetworkPresence presence = new LocalPresence();
    private final NetworkTeleporter teleporter;

    public LocalNetworkBridge(Plugin plugin) {
        this.teleporter = new LocalTeleporter(plugin);
    }

    @Override
    public String serverName() {
        return SERVER_NAME;
    }

    @Override
    public NetworkMessenger messenger() {
        return messenger;
    }

    @Override
    public NetworkPresence presence() {
        return presence;
    }

    @Override
    public NetworkTeleporter teleporter() {
        return teleporter;
    }

    @Override
    public void close() {
    }
}
