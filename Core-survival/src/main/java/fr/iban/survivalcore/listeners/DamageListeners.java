package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.UUID;

public class DamageListeners implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent e) {
		if(e instanceof EntityDamageByEntityEvent event) {
			if(e.getEntity() instanceof Player damaged) {
				Player damager = getPlayerDamager(event);
				if(damager != null && !canPVP(damaged.getUniqueId(), damager.getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
	}

	private Player getPlayerDamager(EntityDamageByEntityEvent event) {
		Player player = null;
		if(event.getCause() == DamageCause.PROJECTILE && event.getDamager() instanceof Projectile projectile) {
			if(projectile.getShooter() instanceof Player) {
				player = (Player)projectile.getShooter();
			}
		}
		if(event.getDamager() instanceof Player) {
			player = (Player) event.getDamager();
		}
		return player;
	}

	private boolean canPVP(UUID p1, UUID p2) {
		PlayerManager playerManager = CoreBukkitPlugin.getInstance().getPlayerManager();
		MSPlayerProfile profile1 = playerManager.getProfile(p1);
		MSPlayerProfile profile2 = playerManager.getProfile(p2);

		return Boolean.TRUE.equals(profile1.getOption(Option.PVP))
				&& Boolean.TRUE.equals(profile2.getOption(Option.PVP));
	}
}