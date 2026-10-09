package fr.iban.network.mscore;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.model.MSPlayer;
import fr.iban.network.NetworkPlayer;
import fr.iban.network.NetworkPresence;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class MSCorePresence implements NetworkPresence {

    private final CoreBukkitPlugin core;

    MSCorePresence(CoreBukkitPlugin core) {
        this.core = core;
    }

    @Override
    public boolean isOnline(UUID uniqueId) {
        return core.getPlayerManager().isOnline(uniqueId);
    }

    @Override
    public Set<String> onlinePlayerNames() {
        return core.getPlayerManager().getOnlinePlayerNames();
    }

    @Override
    public Optional<NetworkPlayer> findPlayer(UUID uniqueId) {
        return toNetworkPlayer(core.getPlayerManager().getOfflinePlayer(uniqueId));
    }

    @Override
    public Optional<NetworkPlayer> findPlayer(String name) {
        MSPlayer player = core.getPlayerManager().getOfflinePlayer(name);
        if (player == null) {
            // The offline index is case-sensitive, online profiles are matched ignoring case.
            player = core.getPlayerManager().getProfile(name);
        }
        return toNetworkPlayer(player);
    }

    @Override
    public void sendMessage(UUID uniqueId, Component message) {
        core.getPlayerManager().sendMessageIfOnline(uniqueId, message);
    }

    private Optional<NetworkPlayer> toNetworkPlayer(MSPlayer player) {
        if (player == null || player.getName() == null) {
            return Optional.empty();
        }
        return Optional.of(new NetworkPlayer(player.getUniqueId(), player.getName()));
    }
}
