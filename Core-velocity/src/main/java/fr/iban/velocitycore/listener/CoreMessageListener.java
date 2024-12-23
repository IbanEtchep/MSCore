package fr.iban.velocitycore.listener;

import com.google.gson.Gson;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.Message;
import fr.iban.common.messaging.message.EventAnnounce;
import fr.iban.common.messaging.message.PlayerSLocationMessage;
import fr.iban.common.teleport.RequestType;
import fr.iban.common.teleport.TeleportToLocation;
import fr.iban.common.teleport.TeleportToPlayer;
import fr.iban.common.teleport.TpRequest;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.event.CoreMessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


import java.util.UUID;

public class CoreMessageListener {

    private final CoreVelocityPlugin plugin;
    private final ProxyServer server;
    private final Gson gson = new Gson();

    public CoreMessageListener(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Subscribe
    public void oneCoreMessage(CoreMessageEvent event) {
        Message message = event.message();

        switch (message.getChannel()) {
            case "EventAnnounce" -> consumeAnnounceMessage(message);
            case "DeathLocation" -> consumeDeathLocationMessage(message);
            case "LastRTPLocation" -> consumeLastRTPLocationMessage(message);
            case "TeleportToLocationBungee" -> consumeTeleportToLocationBungeeMessage(message);
            case "TeleportToPlayerBungee" -> consumeTeleportToPlayerBungeeMessage(message);
            case "TeleportRequestBungee" -> consumeTeleportRequestBungeeMessage(message);
            case CoreChannel.SYNC_PLAYER_CHANNEL ->
                    plugin.getPlayerManager().loadProfile(UUID.fromString(message.getMessage()));
            case CoreChannel.REMOVE_PENDING_TP_CHANNEL ->
                    plugin.getTeleportManager().getPendingTeleports().remove(UUID.fromString(message.getMessage()));
            case CoreChannel.REMOVE_TP_REQUEST_CHANNEL -> consumeRemoveTpRequestMessage(message);
        }
    }


    public void consumeRemoveTpRequestMessage(Message message) {
        TpRequest request = gson.fromJson(message.getMessage(), TpRequest.class);
        plugin.getTeleportManager().getTpRequests().remove(request.getPlayerTo(), request);
    }

    private void consumeTeleportToLocationBungeeMessage(Message message) {
        TeleportToLocation teleportToLocation = gson.fromJson(message.getMessage(), TeleportToLocation.class);
        Player player = plugin.getServer().getPlayer(teleportToLocation.getUuid()).orElse(null);

        if (player != null) {
            if (teleportToLocation.getDelay() == 0) {
                plugin.getTeleportManager().teleport(player, teleportToLocation.getLocation());
            } else {
                plugin.getTeleportManager().delayedTeleport(player, teleportToLocation.getLocation(), Math.abs(teleportToLocation.getDelay()));
            }
        }
    }

    private void consumeTeleportToPlayerBungeeMessage(Message message) {
        TeleportToPlayer teleportToPlayer = gson.fromJson(message.getMessage(), TeleportToPlayer.class);
        Player player = plugin.getServer().getPlayer(teleportToPlayer.getUuid()).orElse(null);
        Player target = plugin.getServer().getPlayer(teleportToPlayer.getTargetId()).orElse(null);

        if (player == null || target == null) {
            return;
        }

        if (teleportToPlayer.getDelay() == 0 || player.hasPermission("bungeeteleport.instant")) {
            plugin.getTeleportManager().teleport(player, target);
        } else {
            plugin.getTeleportManager().delayedTeleport(player, target, Math.abs(teleportToPlayer.getDelay()));
        }
    }


    private void consumeTeleportRequestBungeeMessage(Message message) {
        TpRequest request = gson.fromJson(message.getMessage(), TpRequest.class);
        Player playerFrom = plugin.getServer().getPlayer(request.getPlayerFrom()).orElse(null);
        Player playerTo = plugin.getServer().getPlayer(request.getPlayerTo()).orElse(null);

        if (playerFrom == null || playerTo == null) {
            return;
        }

        System.out.println("Received request from " + playerFrom.getUsername() + " to " + playerTo.getUsername() + " with type " + request.getRequestType());

        if (request.getRequestType() == RequestType.TP) {
            plugin.getTeleportManager().sendTeleportRequest(playerFrom, playerTo);
        } else if (request.getRequestType() == RequestType.TPHERE) {
            plugin.getTeleportManager().sendTeleportHereRequest(playerFrom, playerTo);
        }
    }

    private void consumeDeathLocationMessage(Message message) {
        PlayerSLocationMessage deathLocation = gson.fromJson(message.getMessage(), PlayerSLocationMessage.class);
        plugin.getTeleportManager().getDeathLocations().put(deathLocation.getUuid(), deathLocation.getLocation());
    }

    private void consumeLastRTPLocationMessage(Message message) {
        PlayerSLocationMessage lastRTPLocation = gson.fromJson(message.getMessage(), PlayerSLocationMessage.class);
        plugin.getTeleportManager().getLastRTPLocations().put(lastRTPLocation.getUuid(), lastRTPLocation.getLocation());
    }

    private void consumeAnnounceMessage(Message message) {
        EventAnnounce announce = gson.fromJson(message.getMessage(), EventAnnounce.class);
        String key = announce.getName() + ":" + announce.getArena();

        if (announce.getLocation() == null) {
            if (plugin.getCurrentEvents().containsKey(key)) {
                plugin.getCurrentEvents().remove(key);
                return;
            }
        }

        if (!plugin.getCurrentEvents().containsKey(key)) {
            plugin.getCurrentEvents().put(key, announce.getLocation());
            server.sendMessage(Component.text(announce.getHostName() + " a lancé un event " + announce.getName(), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        }

        broadcastLine();
        server.sendMessage(getCenteredText(" " + announce.getName() + " ", TextDecoration.BOLD));
        server.sendMessage(Component.text(announce.getDesc(), NamedTextColor.WHITE));
        server.sendMessage(Component.text("Arene : " + announce.getArena(), NamedTextColor.WHITE));
        server.sendMessage(Component.text("Cliquez pour rejoindre ou tapez /joinevent", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/joinevent " + key)));
        broadcastLine();
    }


    /*
    UTILS
     */


    private void broadcastLine() {
        String line = "-".repeat(Math.max(0, 30));
        plugin.getServer().sendMessage(Component.text(line, NamedTextColor.GRAY));
    }

    private Component getCenteredText(String text, TextDecoration... decorations) {
        String padding = " ".repeat(Math.max(0, (30 - text.length()) / 2));
        return Component.text(padding + text + padding, NamedTextColor.WHITE).decorate(decorations);
    }
}
