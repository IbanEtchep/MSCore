package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.rewards.RewardsDAO;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.bukkitcore.utils.Scheduler;
import fr.iban.common.manager.GlobalLoggerManager;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.message.PlayerStringMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class JoinQuitListeners implements Listener {

    private final CoreBukkitPlugin plugin;


    public JoinQuitListeners(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        e.setJoinMessage(null);
        if (plugin.getServerName().equals("null")) {
            Scheduler.runLater(() -> PluginMessageHelper.askServerName(player), 10L);
        }
        RewardsDAO.getRewardsAsync(player.getUniqueId()).thenAccept(list -> {
            if (!list.isEmpty()) {
                player.sendMessage("§aVous avez une ou plusieurs récompenses en attente ! (recompenses)");
            }
        });

        GlobalLoggerManager.saveLog(plugin.getServerName(), player.getName() + " (" + Objects.requireNonNull(player.getAddress()).getHostString() + ") logged in at " + player.getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage(null);
        plugin.getTextInputs().remove(player.getUniqueId());
        if (plugin.getServerName().toLowerCase().startsWith("survie")) {
            plugin.getMessagingManager().sendMessage(CoreChannel.LAST_SURVIVAL_SERVER, new PlayerStringMessage(player.getUniqueId(), plugin.getServerName()));
        }

        GlobalLoggerManager.saveLog(plugin.getServerName(), player.getName() + " (" + Objects.requireNonNull(player.getAddress()).getHostString() + ") logged out at " + player.getLocation());
    }

}
