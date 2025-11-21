package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.event.PlayerPreTeleportEvent;
import fr.iban.bukkitcore.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeleportListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerPreTeleportEvent event) {
        Player player = event.getPlayer();

        if(player == null) {
            return;
        }

        if(player.getFallDistance() > 10) {
            player.sendMessage(Lang.get("teleport.no-while-falling"));
            event.setCancelled(true);
            return;
        }

        if(player.hasPermission("servercore.tp.instant")) {
            event.setDelay(0);
        }
    }

}
