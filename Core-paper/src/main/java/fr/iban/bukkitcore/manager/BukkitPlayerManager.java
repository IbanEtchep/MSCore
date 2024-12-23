package fr.iban.bukkitcore.manager;

import fr.iban.common.manager.PlayerManager;
import fr.iban.common.messaging.AbstractMessagingManager;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.message.PlayerStringMessage;
import fr.iban.common.model.MSPlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitPlayerManager extends PlayerManager {

    public BukkitPlayerManager(AbstractMessagingManager messagingManager) {
        super(messagingManager);
    }

    public void sendMessageIfOnline(UUID uuid, Component message) {
        Player player = Bukkit.getPlayer(uuid);

        if(player != null) {
            player.sendMessage(message);
        }else if (isOnline(uuid)) {
            final String jsonText = JSONComponentSerializer.json().serialize(message);
            messagingManager.sendMessage(CoreChannel.SEND_COMPONENT_MESSAGE_TO_PLAYER, new PlayerStringMessage(uuid, jsonText));
        }
    }

    public void sendMessage(MSPlayerProfile player, Component message) {
        sendMessageIfOnline(player.getUniqueId(), message);
    }

    public void broadcastMessage(Component message) {
        Set<UUID> onlinePlayers = new HashSet<>(this.profiles.keySet());
        onlinePlayers.forEach(uuid -> sendMessageIfOnline(uuid, message));
    }
}
