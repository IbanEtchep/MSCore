package fr.iban.bukkitcore.listeners;

import com.earth2me.essentials.User;
import com.google.gson.Gson;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.Message;
import fr.iban.common.messaging.message.PlayerBoolean;
import fr.iban.common.messaging.message.PlayerInfo;
import fr.iban.common.messaging.message.PlayerStringMessage;
import fr.iban.common.teleport.RandomTeleportMessage;
import fr.iban.common.teleport.TeleportToLocation;
import fr.iban.common.teleport.TeleportToPlayer;
import fr.iban.common.teleport.TpRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class CoreMessageListener implements Listener {

    private final CoreBukkitPlugin plugin;
    private final Gson gson = new Gson();
    private final JSONComponentSerializer componentSerializer = JSONComponentSerializer.json();

    public CoreMessageListener(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCoreMessage(CoreMessageEvent e) {
        Message message = e.getMessage();

        switch (e.getMessage().getChannel()) {
            case CoreChannel.SYNC_ACCOUNT_CHANNEL ->
                    plugin.getAccountManager().reloadAccount(UUID.fromString(message.getMessage()));
            case "TeleportToLocationBukkit" -> consumeTeleportToLocationBukkitMessage(message);
            case "TeleportToPlayerBukkit" -> consumeTeleportToPlayerBukkitMessage(message);
            case CoreChannel.ADD_PENDING_TP_CHANNEL ->
                    plugin.getTeleportManager().getPendingTeleports().add(UUID.fromString(e.getMessage().getMessage()));
            case CoreChannel.REMOVE_PENDING_TP_CHANNEL ->
                    plugin.getTeleportManager().getPendingTeleports().remove(UUID.fromString(e.getMessage().getMessage()));
            case CoreChannel.ADD_TP_REQUEST_CHANNEL -> consumeAddTpRequestMessage(message);
            case CoreChannel.REMOVE_TP_REQUEST_CHANNEL -> consumeRemoveTpRequestMessage(message);
            case CoreChannel.PLAYER_JOIN_CHANNEL -> consumePlayerJoinMessage(message);
            case CoreChannel.PLAYER_QUIT_CHANNEL -> consumePlayerQuitMessage(message);
            case CoreChannel.VANISH_STATUS_CHANGE_CHANNEL -> consumeVanishStatusChangeMessage(message);
            case CoreChannel.LAST_SURVIVAL_SERVER -> consumeLastSurvivalServerMessage(message);
            case CoreChannel.RANDOM_TELEPORT ->
                    plugin.getTeleportManager().performRandomTeleport(message.getMessage(RandomTeleportMessage.class));
            case CoreChannel.SYNC_KIT_CLAIM -> consumeKitClaimMessage(message);
            case CoreChannel.SEND_COMPONENT_MESSAGE_TO_PLAYER -> consumeComponentToPlayerMessage(message);
        }
    }

    public void consumePlayerJoinMessage(Message message) {
        PlayerInfo playerMessage = gson.fromJson(message.getMessage(), PlayerInfo.class);
        plugin.getPlayerManager().getOnlinePlayers().put(playerMessage.getUuid(), playerMessage.getName());
        plugin.getPlayerManager().getOfflinePlayers().putIfAbsent(playerMessage.getName(), playerMessage.getUuid());
    }

    public void consumePlayerQuitMessage(Message message) {
        plugin.getPlayerManager().getOnlinePlayers().remove(UUID.fromString(message.getMessage()));
    }

    public void consumeAddTpRequestMessage(Message message) {
        TpRequest request = gson.fromJson(message.getMessage(), TpRequest.class);
        plugin.getTeleportManager().getTpRequests().put(request.getPlayerTo(), request);
    }

    public void consumeRemoveTpRequestMessage(Message message) {
        TpRequest request = gson.fromJson(message.getMessage(), TpRequest.class);
        plugin.getTeleportManager().getTpRequests().remove(request.getPlayerTo(), request);
    }

    private void consumeTeleportToLocationBukkitMessage(Message message) {
        TeleportToLocation ttl = gson.fromJson(message.getMessage(), TeleportToLocation.class);
        plugin.getTeleportManager().performTeleportToLocation(ttl);
    }

    private void consumeTeleportToPlayerBukkitMessage(Message message) {
        TeleportToPlayer ttp = gson.fromJson(message.getMessage(), TeleportToPlayer.class);
        plugin.getTeleportManager().performTeleportToPlayer(ttp);
    }

    private void consumeVanishStatusChangeMessage(Message message) {
        PlayerBoolean playerBoolean = message.getMessage(PlayerBoolean.class);
        plugin.getPlayerManager().setVanished(playerBoolean.getUuid(), playerBoolean.isValue());
    }

    private void consumeLastSurvivalServerMessage(Message message) {
        PlayerStringMessage msg = message.getMessage(PlayerStringMessage.class);
        plugin.getServerManager().setLastSurvivalServer(msg.uuid(), msg.string());
    }

    private void consumeKitClaimMessage(Message message) {
        PlayerStringMessage msg = message.getMessage(PlayerStringMessage.class);
        if (plugin.getEssentials() != null) {
            User user = plugin.getEssentials().getUser(msg.uuid());
            if (user == null) return;
            final Calendar time = new GregorianCalendar();
            user.setKitTimestamp(msg.string(), time.getTimeInMillis());
        }
    }

    private void consumeComponentToPlayerMessage(Message message) {
        PlayerStringMessage msg = message.getMessage(PlayerStringMessage.class);
        Player player = Bukkit.getPlayer(msg.uuid());

        if(player == null) return;

        Component component = componentSerializer.deserialize(msg.string());
        player.sendMessage(component);
    }
}