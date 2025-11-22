package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.SLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
	
	private final CoreBukkitPlugin plugin;
	
	public DeathListener(CoreBukkitPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		Location loc = player.getLocation();
		
		if(plugin.getServerName() == null) {
			return;
		}

		MSPlayerProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
		SLocation deathLoc = SLocationUtils.getSLocation(loc);
		profile.setDeathLocation(deathLoc);
		plugin.getPlayerManager().saveProfile(profile);
	}

}
