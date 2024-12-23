package fr.iban.bukkitcore.manager;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.PlayerPreTeleportEvent;
import fr.iban.bukkitcore.plan.PlanDataManager;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TeleportManager {

    private final CoreBukkitPlugin plugin;
    private final ListMultimap<UUID, TpRequest> tpRequests = ArrayListMultimap.create();
    private final List<UUID> pendingTeleports = new ArrayList<>();
    private final Map<UUID, Location> unsafeTpPending = new HashMap<>();

    public TeleportManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleport player to SLocation
     */
    public void teleport(Player player, SLocation sloc) {
        teleport(player, sloc, 0);
    }

    /**
     * Delayed teleport player to SLocation.
     *
     * @param delay delay in seconds
     */
    public void teleport(Player player, SLocation sloc, int delay) {
        UUID uuid = player.getUniqueId();
        PlayerPreTeleportEvent teleportEvent = new PlayerPreTeleportEvent(player, delay);
        if (teleportEvent.callEvent()) {
            setLastLocation(uuid);
            plugin.getMessagingManager().sendMessage("TeleportToLocationBungee", new TeleportToLocation(uuid, sloc, teleportEvent.getDelay()));
        }
    }

    /**
     * Teleport player to another player
     *
     * @param uuid - player's uuid
     */
    public void teleport(UUID uuid, UUID target) {
        teleport(uuid, target, 0);
    }

    /**
     * Delayed teleport player to another player
     *
     * @param uuid   - player's uuid
     * @param target - target player's uuid
     * @param delay  in seconds
     */
    public void teleport(UUID uuid, UUID target, int delay) {
        Player player = Bukkit.getPlayer(uuid);
        PlayerPreTeleportEvent teleportEvent = new PlayerPreTeleportEvent(player, delay);
        if (teleportEvent.callEvent()) {
            setLastLocation(uuid);
            plugin.getMessagingManager().sendMessage("TeleportToPlayerBungee", new TeleportToPlayer(uuid, target, teleportEvent.getDelay()));
        }
    }

    public void teleport(Player player, String server) {
        teleport(player, server, 0);
    }

    public void teleport(Player player, String server, int delay) {
        PlayerPreTeleportEvent teleportEvent = new PlayerPreTeleportEvent(player, delay);

        if (teleportEvent.callEvent()) {
            if (teleportEvent.getDelay() <= 0) {
                setLastLocation(player.getUniqueId());
                PluginMessageHelper.sendPlayerToServer(player, server);
            } else {
                player.sendMessage("§aTéléportation dans " + delay + " secondes. §cNe bougez pas !");
                pendingTeleports.add(player.getUniqueId());
                plugin.getScheduler().runLater(task -> {
                    if (isTeleportWaiting(player.getUniqueId())) {
                        setLastLocation(player.getUniqueId());
                        PluginMessageHelper.sendPlayerToServer(player, server);
                    }
                }, delay, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Used for essentials /back
     */
    private void setLastLocation(UUID uuid) {
        Essentials essentials = plugin.getEssentials();

        if (essentials != null) {
            User user = essentials.getUser(uuid);
            if (user != null) {
                user.setLastLocation();
            }
        }
    }

    /**
     * Send teleport request to player.
     */
    public void sendTeleportRequest(UUID from, UUID to, RequestType type) {
        plugin.getMessagingManager().sendMessage("TeleportRequestBungee", new TpRequest(from, to, type));
    }

    public List<UUID> getPendingTeleports() {
        return pendingTeleports;
    }

    public void removeTeleportWaiting(UUID uuid) {
        pendingTeleports.remove(uuid);
        plugin.getMessagingManager().sendMessage(CoreChannel.REMOVE_PENDING_TP_CHANNEL, uuid.toString());
    }

    public boolean isTeleportWaiting(UUID uuid) {
        return pendingTeleports.contains(uuid);
    }

    public ListMultimap<UUID, TpRequest> getTpRequests() {
        return tpRequests;
    }

    public List<TpRequest> getTpRequests(Player player) {
        return tpRequests.get(player.getUniqueId());
    }

    public TpRequest getTpRequestFrom(Player player, UUID from) {
        for (TpRequest request : getTpRequests(player)) {
            if (request.getPlayerFrom().equals(from)) {
                return request;
            }
        }
        return null;
    }

    public void removeTpRequest(UUID uuid, TpRequest tpRequest) {
        tpRequests.remove(uuid, tpRequest);
        plugin.getMessagingManager().sendMessage(CoreChannel.REMOVE_TP_REQUEST_CHANNEL, tpRequest);
    }

    public void performTeleportToLocation(TeleportToLocation teleportToLocation) {
        if (!teleportToLocation.getLocation().getServer().equalsIgnoreCase(CoreBukkitPlugin.getInstance().getServerName())) {
            return;
        }

        SLocation sloc = teleportToLocation.getLocation();
        Location loc = SLocationUtils.getLocation(sloc);

        AtomicInteger count = new AtomicInteger();
        plugin.getScheduler().runTimer(task -> {
            Player player = Bukkit.getPlayer(teleportToLocation.getUuid());

            if (player != null) {
                tpAsync(player, loc);
                task.cancel();
            }

            count.getAndIncrement();

            if (count.get() > 20) {
                task.cancel();
            }
        }, 1L, 1L);
    }

    public void performTeleportToPlayer(TeleportToPlayer teleportToPlayer) {
        Player target = Bukkit.getPlayer(teleportToPlayer.getTargetId());

        if (target == null) {
            return;
        }

        plugin.getScheduler().runTimer(task -> {
            Player updatedTarget = Bukkit.getPlayer(teleportToPlayer.getTargetId());
            Player player = Bukkit.getPlayer(teleportToPlayer.getUuid());

            if (updatedTarget == null) {
                task.cancel();
                return;
            }

            if (player != null) {
                tpAsync(player, updatedTarget.getLocation());
                task.cancel();
            }

        }, 1L, 1L);
    }

    public void performRandomTeleport(RandomTeleportMessage rtpMessage) {
        if (!plugin.getServerName().equalsIgnoreCase(rtpMessage.getTargetServer())) {
            return;
        }

        String world = switch (rtpMessage.getWorld()) {
            case "world" -> Bukkit.getWorlds().get(0).getName();
            case "world_nether" -> Bukkit.getWorlds().get(1).getName();
            case "world_the_end" -> Bukkit.getWorlds().get(2).getName();
            default -> rtpMessage.getWorld();
        };

        AtomicInteger count = new AtomicInteger();
        plugin.getScheduler().runTimer(task -> {
            Player player = Bukkit.getPlayer(rtpMessage.getUuid());

            if (player != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName() + " " + world);
                task.cancel();
            }

            count.getAndIncrement();

            if (count.get() > 200) {
                task.cancel();
            }
        }, 1L, 1L);

    }


    private void tpAsync(Player player, Location loc) {
        player.sendActionBar("§aChargement de la zone...");
        loc.getWorld().getChunkAtAsyncUrgently(loc).thenAccept(chunk -> {
            if (SLocationUtils.isSafeLocation(loc) || player.getGameMode() != GameMode.SURVIVAL) {
                player.teleportAsync(loc).thenAccept(result -> {
                    if (result) {
                        player.sendActionBar("§aTéléportation effectuée !");
                    } else {
                        player.sendActionBar("§cLa téléportation a échoué !");
                    }
                });
            } else {
                unsafeTpPending.put(player.getUniqueId(), loc);
                player.sendMessage(Component.text("⚠ La zone de téléportation n'est pas sécurisée.\nSi vous souhaitez tout de même vous y téléporter, cliquez ici. (ou tapez /tplastunsafe)")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/tplastunsafe"))
                        .hoverEvent(HoverEvent.showText(Component.text("Se téléporter à vos risques et périls.", NamedTextColor.WHITE).decorate(TextDecoration.BOLD))));
            }
        });
    }

    public void tpAsyncLastUnsafe(Player player) {
        Location location = unsafeTpPending.get(player.getUniqueId());
        if (location != null) {
            player.teleportAsync(location).thenAccept(result -> {
                if (result) {
                    player.sendActionBar("§aTéléportation effectuée !");
                } else {
                    player.sendActionBar("§cLa téléportation a échoué !");
                }
            });
            unsafeTpPending.remove(player.getUniqueId());
        } else {
            player.sendMessage("§cVous n'avez pas de téléportation en attente.");
        }
    }

    public void teleportToSurvivalServer(Player player, String server) {
        ServerManager serverManager = plugin.getServerManager();
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (server == null) {
            if (!plugin.getServerManager().isSurvivalServer()) {
                String lastSurvivalServer = profile.getLastSurvivalLocation().getServer();

                if (lastSurvivalServer != null) {
                    server = lastSurvivalServer;
                } else {
                    server = serverManager.getDefaultSurvivalServer();
                }
            } else {
                player.sendMessage("§cVous êtes déjà dans un serveur survie.");
                return;
            }
        }

        teleport(player, server);
    }

    public void randomTeleportToSurvivalServer(Player player) {
        randomTeleport(player, determineTargetServer(plugin.getServerManager().getSurvivalServers()), "world");
    }

    public void randomTeleport(Player player, String server, String world) {
        RandomTeleportMessage randomTeleportMessage = new RandomTeleportMessage(player.getUniqueId(), server, world);
        if (plugin.getServerName().equalsIgnoreCase(randomTeleportMessage.getTargetServer())) {
            performRandomTeleport(randomTeleportMessage);
        } else {
            plugin.getMessagingManager().sendMessage(CoreChannel.RANDOM_TELEPORT, randomTeleportMessage);
            teleport(player, server, 3);
        }
    }

    /**
     * Get the less played server or random if plan is not hooked
     *
     * @param possibleTargets possible target server names
     * @return best server
     */
    private String determineTargetServer(List<String> possibleTargets) {
        PlanDataManager planDataManager = plugin.getPlanDataManager();
        boolean isPlanRtpBalancerEnabled = plugin.getConfig().getBoolean("plan-rtp-balancer", false);

        if (planDataManager.usePlanIntegration() && isPlanRtpBalancerEnabled) {
            String server = planDataManager.getServerWithLowestPlayTime(possibleTargets);
            if (server != null) {
                return server;
            }
        }

        Collections.shuffle(possibleTargets);
        return possibleTargets.get(0);
    }
}