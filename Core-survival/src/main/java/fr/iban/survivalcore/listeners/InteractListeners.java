package fr.iban.survivalcore.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;


public class InteractListeners implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		Block clickedBlock = e.getClickedBlock();
		if (clickedBlock != null && item != null
				&& clickedBlock.getType() == Material.SPAWNER
				&& item.getItemMeta() instanceof SpawnEggMeta
				&& !player.hasPermission("servercore.changespawner")) {
			e.setCancelled(true);
		}
	}
}