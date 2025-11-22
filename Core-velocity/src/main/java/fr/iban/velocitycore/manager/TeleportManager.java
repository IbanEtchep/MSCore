package fr.iban.velocitycore.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.themoep.minedown.adventure.MineDown;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.teleport.*;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TeleportManager {

    private final CoreVelocityPlugin plugin;
    private final List<UUID> pendingTeleports = new ArrayList<>();
    private final ListMultimap<UUID, TpRequest> tpRequests = ArrayListMultimap.create();


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

        player.sendMessage(MineDown.parse(
                MessageBuilder.translatable(LangKey.TP_DELAYED)
                        .placeholder("delay", String.valueOf(delay))
                        .toStringRaw()
        ));

        if (isTeleportWaiting(player)) {
            player.sendMessage(MineDown.parse(
                    MessageBuilder.translatable(LangKey.TP_ALREADY_WAITING).toStringRaw()
            ));
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

        player.sendMessage(MineDown.parse(
                MessageBuilder.translatable(LangKey.TP_DELAYED)
                        .placeholder("delay", String.valueOf(delay))
                        .toStringRaw()
        ));

        if (isTeleportWaiting(player)) {
            player.sendMessage(MineDown.parse(
                    MessageBuilder.translatable(LangKey.TP_ALREADY_WAITING).toStringRaw()
            ));
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
        from.sendMessage(MineDown.parse(
                MessageBuilder.translatable(LangKey.TP_REQUEST_SENT).toStringRaw()
        ));

        String minedownMessage = MessageBuilder.translatable(LangKey.TP_REQUEST_TO_PLAYER)
                .placeholder("fromName", from.getUsername())
                .toStringRaw();

        to.sendMessage(MineDown.parse(minedownMessage));

        TpRequest req = getTpRequestFrom(from, to);
        if (req != null) {
            removeTpRequest(to.getUniqueId(), req);
        }

        addTpRequest(to.getUniqueId(), new TpRequest(from.getUniqueId(), to.getUniqueId(), RequestType.TP));

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            TpRequest req2 = getTpRequestFrom(from, to);
            if (req2 != null) {
                removeTpRequest(to.getUniqueId(), req2);
                from.sendMessage(MineDown.parse(
                        MessageBuilder.translatable(LangKey.TP_REQUEST_EXPIRED)
                                .placeholder("player", to.getUsername())
                                .toStringRaw()
                ));
            }
        }).delay(2, TimeUnit.MINUTES).schedule();
    }

    public void sendTeleportHereRequest(Player from, Player to) {
        from.sendMessage(MineDown.parse(
                MessageBuilder.translatable(LangKey.TP_REQUEST_SENT).toStringRaw()
        ));

        String minedownMessage = MessageBuilder.translatable(LangKey.TP_REQUEST_HERE)
                .placeholder("fromName", from.getUsername())
                .toStringRaw();

        to.sendMessage(MineDown.parse(minedownMessage));

        TpRequest req = getTpRequestFrom(to, from);
        if (req != null) {
            removeTpRequest(to.getUniqueId(), req);
        }

        addTpRequest(to.getUniqueId(), new TpRequest(from.getUniqueId(), to.getUniqueId(), RequestType.TPHERE));

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            TpRequest req2 = getTpRequestFrom(to, from);
            if (req2 != null) {
                removeTpRequest(to.getUniqueId(), req2);
                from.sendMessage(MineDown.parse(
                        MessageBuilder.translatable(LangKey.TP_REQUEST_EXPIRED)
                                .placeholder("player", to.getUsername())
                                .toStringRaw()
                ));
            }
        }).delay(2, TimeUnit.MINUTES).schedule();
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
