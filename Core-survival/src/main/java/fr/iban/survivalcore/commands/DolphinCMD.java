package fr.iban.survivalcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.iban.bukkitcore.utils.Lang;

public class DolphinCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(player.hasPermission("servercore.dolphin")) {
				if(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
					player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
					player.sendMessage(Lang.get("dolphin.disabled"));
				}else {
					player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0));
					player.sendMessage(Lang.get("dolphin.enabled"));
				}
			}
		}
		return false;
	}
}
