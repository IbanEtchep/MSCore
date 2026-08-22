package fr.iban.velocitycore.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.teleport.*;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TeleportManager {

    private final CoreVelocityPlugin plugin;
    private final List<UUID> pendingTeleports = new ArrayList<>();
    private final ListMultimap<UUID, TpRequest> tpRequests = ArrayListMultimap.create();

    private static final String SEPARATOR = "<gray><st>---------------------------------------------</st></gray>";
    private static final String TP_REQUEST_SENT = "<green>Requête de téléportation envoyée, en attente d'une réponse...";
    private static final String TP_REQUEST_EXPIRED = "<red>Votre requête de téléportation envoyée à <player> a expiré.";
    private static final String TP_ALREADY_WAITING = "<red>Une seule téléportation à la fois !";
    private static final String TP_COUNTDOWN = "<green>Téléportation dans <delay> secondes. <red>Ne bougez pas !";


    public TeleportManager(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, SLocation location) {
        ProxyServer proxy = plugin.getServer();
        RegisteredServer targetServer = proxy.getServer(location.getServer()).orElse(null);
        ServerConnection currentServer = player.getCurrentServer().orElse(null);

        if (targetServer == null || currentServer == null) {
            return;
        }

        ServerInfo serverInfo = targetServer.getServerInfo();

        if (serverInfo.getName().equals(currentServer.getServerInfo().getName())) {
            plugin.getMessagingManager().sendMessage("TeleportToLocationBukkit", new TeleportToLocation(player.getUniqueId(), location));
        } else {
            player.createConnectionRequest(targetServer).connect().thenAcceptAsync((result) -> {
                if (result.isSuccessful()) {
                    plugin.getMessagingManager().sendMessage("TeleportToLocationBukkit", new TeleportToLocation(player.getUniqueId(), location));
                }
            });
        }
    }

    public void delayedTeleport(Player player, SLocation location, int delay) {
        if (player.hasPermission("servercore.tp.instant")) {
            teleport(player, location);
            return;
        }

        player.sendMessage(countdown(delay));
        if (isTeleportWaiting(player)) {
            player.sendMessage(mm(TP_ALREADY_WAITING));
            return;
        }

        setTeleportWaiting(player);

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (isTeleportWaiting(player)) {
                teleport(player, location);
                removeTeleportWaiting(player.getUniqueId());
            }
        }).delay(delay, TimeUnit.SECONDS).schedule();
    }

    public void teleport(Player player, Player target) {
        ServerConnection currentServer = player.getCurrentServer().orElse(null);
        ServerConnection targetServer = target.getCurrentServer().orElse(null);

        if (targetServer == null || currentServer == null) {
            return;
        }

        if (targetServer.getServerInfo().getName().equals(currentServer.getServerInfo().getName())) {
            plugin.getMessagingManager().sendMessage("TeleportToPlayerBukkit", new TeleportToPlayer(player.getUniqueId(), target.getUniqueId()));
        } else {
            player.createConnectionRequest(targetServer.getServer()).connect().thenAcceptAsync((result) -> {
                if (result.isSuccessful()) {
                    plugin.getMessagingManager().sendMessage("TeleportToPlayerBukkit", new TeleportToPlayer(player.getUniqueId(), target.getUniqueId()));
                }
            });
        }
    }


    public void delayedTeleport(Player player, Player target, int delay) {
        player.sendMessage(countdown(delay));
        if (isTeleportWaiting(player)) {
            player.sendMessage(mm(TP_ALREADY_WAITING));
            return;
        }

        setTeleportWaiting(player);

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (isTeleportWaiting(player)) {
                removeTeleportWaiting(player.getUniqueId());
                teleport(player, target);
            }
        }).delay(delay, TimeUnit.SECONDS).schedule();
    }

    public void sendTeleportRequest(Player from, Player to) {
        from.sendMessage(mm(TP_REQUEST_SENT));
        to.sendMessage(buildTpRequest("<gold><player></gold><white> souhaite se téléporter à vous.", from.getUsername()));

        //Retirer la requête déjà existante si il y en a une.
        TpRequest req = getTpRequestFrom(from, to);
        if (req != null) {
            removeTpRequest(to.getUniqueId(), req);
        }

        addTpRequest(to.getUniqueId(), new TpRequest(from.getUniqueId(), to.getUniqueId(), RequestType.TP));

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            TpRequest req2 = getTpRequestFrom(from, to);
            if (req2 != null) {
                removeTpRequest(to.getUniqueId(), req2);
                from.sendMessage(mm(TP_REQUEST_EXPIRED, to.getUsername()));
            }
        }).delay(2, TimeUnit.MINUTES).schedule();
    }

    public void sendTeleportHereRequest(Player from, Player to) {
        from.sendMessage(mm(TP_REQUEST_SENT));
        to.sendMessage(buildTpRequest("<gold><player></gold><white> souhaite que vous vous téléportiez à lui.", from.getUsername()));

        //Retirer la requête déjà existante si il y en a une.
        TpRequest req = getTpRequestFrom(to, from);
        if (req != null) {
            removeTpRequest(to.getUniqueId(), req);
        }

        addTpRequest(to.getUniqueId(), new TpRequest(from.getUniqueId(), to.getUniqueId(), RequestType.TPHERE));

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            TpRequest req2 = getTpRequestFrom(to, from);
            if (req2 != null) {
                removeTpRequest(to.getUniqueId(), req2);
                from.sendMessage(mm(TP_REQUEST_EXPIRED, to.getUsername()));
            }
        }).delay(2, TimeUnit.MINUTES).schedule();
    }

    private Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    private Component mm(String message, String playerName) {
        return MiniMessage.miniMessage().deserialize(message, Placeholder.unparsed("player", playerName));
    }

    private Component countdown(int delay) {
        return MiniMessage.miniMessage().deserialize(TP_COUNTDOWN, Placeholder.unparsed("delay", String.valueOf(delay)));
    }

    private Component buildTpRequest(String line, String fromName) {
        return mm(SEPARATOR)
                .append(Component.newline())
                .append(mm(line, fromName))
                .append(Component.newline())
                .append(buildRequestActions(fromName))
                .append(Component.newline())
                .append(mm(SEPARATOR));
    }

    /**
     * Les boutons sont construits en Java plutot qu'en MiniMessage : un placeholder n'est pas
     * resolu a l'interieur d'un argument de tag (&lt;click:run_command:'...'&gt;), le pseudo doit
     * donc etre injecte dans la commande cote code.
     */
    private Component buildRequestActions(String fromName) {
        return mm("<white>Vous pouvez ")
                .append(action("[ACCEPTER]", NamedTextColor.GREEN, "/tpyes " + fromName, "Cliquez pour accepter la demande"))
                .append(mm("<white> ou "))
                .append(action("[REFUSER]", NamedTextColor.RED, "/tpno " + fromName, "Cliquez pour refuser la demande"))
                .append(mm("<white>."));
    }

    private Component action(String label, NamedTextColor color, String command, String hover) {
        return Component.text(label, color)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(hover, color)));
    }

    public List<UUID> getPendingTeleports() {
        return pendingTeleports;
    }

    public void setTeleportWaiting(Player player) {
        pendingTeleports.add(player.getUniqueId());
        plugin.getMessagingManager().sendMessage(CoreChannel.ADD_PENDING_TP_CHANNEL, player.getUniqueId().toString());
    }

    public void removeTeleportWaiting(UUID uuid) {
        pendingTeleports.remove(uuid);
        plugin.getMessagingManager().sendMessage(CoreChannel.REMOVE_PENDING_TP_CHANNEL, uuid.toString());
    }

    public boolean isTeleportWaiting(Player player) {
        return pendingTeleports.contains(player.getUniqueId());
    }

    public List<TpRequest> getTpRequests(Player player) {
        return tpRequests.get(player.getUniqueId());
    }

    public ListMultimap<UUID, TpRequest> getTpRequests() {
        return tpRequests;
    }

    public TpRequest getTpRequestFrom(Player player, Player from) {
        for (TpRequest request : getTpRequests(player)) {
            if (request.getPlayerFrom().equals(from.getUniqueId())) {
                return request;
            }
        }
        return null;
    }

    public void addTpRequest(UUID uuid, TpRequest tpRequest) {
        tpRequests.put(uuid, tpRequest);
        plugin.getMessagingManager().sendMessage(CoreChannel.ADD_TP_REQUEST_CHANNEL, tpRequest);
    }

    public void removeTpRequest(UUID uuid, TpRequest tpRequest) {
        tpRequests.remove(uuid, tpRequest);
        plugin.getMessagingManager().sendMessage(CoreChannel.REMOVE_TP_REQUEST_CHANNEL, tpRequest);
    }
}
