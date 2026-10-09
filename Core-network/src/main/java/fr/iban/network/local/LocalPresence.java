package fr.iban.network.local;

import fr.iban.network.NetworkPlayer;
import fr.iban.network.NetworkPresence;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class LocalPresence implements NetworkPresence {

    @Override
    public boolean isOnline(UUID uniqueId) {
        return Bukkit.getPlayer(uniqueId) != null;
    }

    @Override
    public Set<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<NetworkPlayer> findPlayer(UUID uniqueId) {
        return toNetworkPlayer(Bukkit.getOfflinePlayer(uniqueId));
    }

    @Override
    public Optional<NetworkPlayer> findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return toNetworkPlayer(online);
        }
        return toNetworkPlayer(Bukkit.getOfflinePlayerIfCached(name));
    }

    @Override
    public void sendMessage(UUID uniqueId, Component message) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    private Optional<NetworkPlayer> toNetworkPlayer(OfflinePlayer player) {
        if (player == null || player.getName() == null || !(player.isOnline() || player.hasPlayedBefore())) {
            return Optional.empty();
        }
        return Optional.of(new NetworkPlayer(player.getUniqueId(), player.getName()));
    }
}
