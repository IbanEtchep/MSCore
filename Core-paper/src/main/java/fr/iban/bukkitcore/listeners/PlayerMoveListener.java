package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.TeleportManager;
import fr.iban.bukkitcore.utils.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final CoreBukkitPlugin plugin;

    public PlayerMoveListener(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        final Location from = e.getFrom();
        final Location to = e.getTo();

        int x = Math.abs(from.getBlockX() - to.getBlockX());
        int y = Math.abs(from.getBlockY() - to.getBlockY());
        int z = Math.abs(from.getBlockZ() - to.getBlockZ());

        if (x == 0 && y == 0 && z == 0) return;

        TeleportManager teleportManager = plugin.getTeleportManager();
        if (teleportManager.getPendingTeleports().contains(player.getUniqueId())) {
            teleportManager.removeTeleportWaiting(player.getUniqueId());
            player.sendMessage(Lang.get("teleport.cancelled-move"));
        }
    }
}
