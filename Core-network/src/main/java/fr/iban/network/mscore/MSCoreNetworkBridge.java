package fr.iban.network.mscore;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.network.NetworkBridge;
import fr.iban.network.NetworkMessenger;
import fr.iban.network.NetworkPresence;
import fr.iban.network.NetworkTeleporter;
import org.bukkit.plugin.Plugin;

/**
 * Delegates to the managers of an enabled MSCore plugin. Messages keep MSCore's wire format,
 * so plugins using this bridge interoperate with plugins still calling MSCore directly.
 */
public class MSCoreNetworkBridge implements NetworkBridge {

    private final CoreBukkitPlugin core;
    private final MSCoreMessenger messenger;
    private final NetworkPresence presence;
    private final NetworkTeleporter teleporter;

    public MSCoreNetworkBridge(Plugin plugin) {
        this.core = CoreBukkitPlugin.getInstance();
        this.messenger = new MSCoreMessenger(plugin, core);
        this.presence = new MSCorePresence(core);
        this.teleporter = new MSCoreTeleporter(core);
    }

    @Override
    public String serverName() {
        return core.getServerName();
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
        messenger.close();
    }
}
