package fr.iban.survivalcore.commands;

import com.earth2me.essentials.utils.DateUtil;
import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.utils.Lang;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedCMD {

	private final SurvivalCorePlugin plugin;
	private final Map<UUID, Long> cooldowns = new HashMap<>();

	public FeedCMD(SurvivalCorePlugin plugin) {
		this.plugin = plugin;
	}

	@Command("feed")
	@CommandPermission("servercore.feed")
	public void feed(Player player) {
		if(hasCooldown(player)) {
			return;
		}

		player.setFoodLevel(20);
		player.setSaturation(5f);
		player.sendMessage(Lang.get("feed.fed"));

		if(getCooldownTime(player) > 0){
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
		}
	}

	private boolean hasCooldown(Player player) {
		if (cooldowns.containsKey(player.getUniqueId())) {
			int cooldownTime = getCooldownFromConfig(player, "feed") * 1000;
			long lastRep = cooldowns.get(player.getUniqueId());
			if (System.currentTimeMillis() - lastRep > cooldownTime) {
				cooldowns.remove(player.getUniqueId());
				return false;
			} else {
				player.sendMessage(Lang.get("feed.cooldown").replace("%time%", DateUtil.formatDateDiff(lastRep+cooldownTime)));
				return true;
			}
		}
		return false;
	}

	public int getCooldownTime(Player player) {
		return player.hasPermission("servercore.feed.bypasscooldown") ? 0 : getCooldownFromConfig(player, "feed");
	}

	private int getCooldownFromConfig(Player player, String command) {
		int minCooldown = Integer.MAX_VALUE;
		List<String> cooldowns = plugin.getConfig().getStringList("cooldowns." + command + ".permissions");
		for (String cooldownString : cooldowns) {
			String[] splitted = cooldownString.split(":");
			String permission = splitted[0];
			int cooldownTime = Integer.parseInt(splitted[1]);
			if (player.hasPermission(permission) && minCooldown > cooldownTime) {
				minCooldown = cooldownTime;
			}
		}
		return minCooldown;
	}

}
