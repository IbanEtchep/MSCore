package fr.iban.network;

import fr.iban.network.local.LocalNetworkBridge;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class NetworkBridges {

    /**
     * Name of the MSCore plugin as declared in its plugin.yml.
     */
    public static final String MSCORE_PLUGIN = "Core";

    public enum Mode {
        /**
         * MSCore if it is enabled, local otherwise.
         */
        AUTO,
        /**
         * Single server, no MSCore.
         */
        LOCAL,
        /**
         * MSCore, fails if it is not enabled.
         */
        MSCORE
    }

    private NetworkBridges() {
    }

    /**
     * Same as {@code create(plugin, Mode.AUTO)}. The calling plugin must declare
     * {@code softdepend: [Core]} (or {@code depend}) so that MSCore is enabled first.
     */
    public static NetworkBridge create(Plugin plugin) {
        return create(plugin, Mode.AUTO);
    }

    public static NetworkBridge create(Plugin plugin, Mode mode) {
        boolean mscoreEnabled = Bukkit.getPluginManager().isPluginEnabled(MSCORE_PLUGIN);

        return switch (mode) {
            case LOCAL -> new LocalNetworkBridge(plugin);
            case MSCORE -> {
                if (!mscoreEnabled) {
                    throw new IllegalStateException("Network mode MSCORE requested but plugin " + MSCORE_PLUGIN + " is not enabled");
                }
                yield MSCoreLoader.load(plugin);
            }
            case AUTO -> mscoreEnabled ? MSCoreLoader.load(plugin) : new LocalNetworkBridge(plugin);
        };
    }

    /**
     * Keeps MSCore classes out of {@link NetworkBridges} so they are only loaded when MSCore is present.
     */
    private static final class MSCoreLoader {
        static NetworkBridge load(Plugin plugin) {
            return new fr.iban.network.mscore.MSCoreNetworkBridge(plugin);
        }
    }
}
