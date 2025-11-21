package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.bukkitcore.rewards.RewardsDAO;
import fr.iban.bukkitcore.utils.Lang;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.manager.GlobalLoggerManager;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.message.PlayerStringMessage;
import fr.iban.common.model.MSPlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;
import java.util.UUID;

public class JoinQuitListeners implements Listener {

    private final CoreBukkitPlugin plugin;

    public JoinQuitListeners(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uniqueId = player.getUniqueId();
        String name = player.getName();

        e.joinMessage(null);

        RewardsDAO.getRewardsAsync(uniqueId).thenAccept(list -> {
            if (!list.isEmpty()) {
                player.sendMessage(Lang.get("join.rewards-pending"));
            }
        });

        PlayerManager playerManager = plugin.getPlayerManager();

        plugin.getScheduler().runLaterAsync(task -> {
            if(playerManager.getProfile(uniqueId) == null) {
                playerManager.loadProfile(uniqueId, name);
                playerManager.handlePlayerJoin(uniqueId);
                plugin.getLogger().info("Loaded profile for " + name + " (" + uniqueId + ")");
            }
        }, 10L);

        GlobalLoggerManager.saveLog(plugin.getServerName(), player.getName() + " (" + Objects.requireNonNull(player.getAddress()).getHostString() + ") logged in at " + player.getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.quitMessage(null);
        plugin.getTextInputs().remove(player.getUniqueId());

        if (plugin.getServerManager().isSurvivalServer()) {
            BukkitPlayerManager playerManager = plugin.getPlayerManager();
            MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());
            profile.setLastSurvivalLocation(SLocationUtils.getSLocation(player.getLocation()));
            playerManager.saveProfile(profile);
        }

        GlobalLoggerManager.saveLog(plugin.getServerName(), player.getName() + " (" + Objects.requireNonNull(player.getAddress()).getHostString() + ") logged out at " + player.getLocation());
    }
}
