package fr.iban.bukkitcore.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.RewardsMenu;
import fr.iban.bukkitcore.rewards.Reward;
import fr.iban.bukkitcore.rewards.RewardsDAO;

public class RecompensesCMD implements CommandExecutor, TabCompleter {

	private final CoreBukkitPlugin plugin;

	public RecompensesCMD(CoreBukkitPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 && sender instanceof Player) {
			Player player = (Player)sender;
			RewardsDAO.getRewardsAsync(player.getUniqueId()).thenAccept(rewards -> {
				if(!rewards.isEmpty()) {
					plugin.getScheduler().runAtEntity(player, task -> new RewardsMenu(player, rewards).open());
				}else {
					player.sendMessage("§cVous n'avez pas de récompense en attente.");
				}
			});
		}else if(args.length >= 1 && sender.hasPermission("servercore.addrewards")) {
			switch (args[0].toLowerCase()) {
			case "give":
				if(args.length == 3) {
					OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(args[1]);
					if(op != null) {
						try {
							int id = Integer.parseInt(args[2]);
							RewardsDAO.getTemplateRewardsAsync().thenAccept(rewards -> {
								for(Reward r : rewards) {
									if(r.id() == id) {
										RewardsDAO.addRewardAsync(op.getUniqueId().toString(), r.name(), r.server(), r.command());
										if(op.isOnline()) {
											Player p = (Player)op;
											p.sendMessage("§aVous avez reçu une récompense (/recompenses).");
										}
									}
								}
							});
						} catch (NumberFormatException e) {
							sender.sendMessage("§cL'id doit être un nombre entier !");
						}
					}else {
						sender.sendMessage("§cJoueur non trouvé.");
					}
				}else {
					sender.sendMessage("/recompenses give pseudo templateID");
				}
				break;
			case "removetemplate":
				if(args.length == 2) {
					try {
						int id = Integer.parseInt(args[1]);
						RewardsDAO.getTemplateRewardsAsync().thenAccept(rewards -> {
							for(Reward r : rewards) {
								if(r.id() == id) {
									RewardsDAO.removeRewardAsync("template", r);
								}
							}
						});
					} catch (NumberFormatException e) {
						sender.sendMessage("§cL'id doit être un nombre entier !");
					}
				}else {
					sender.sendMessage("/recompenses removetemplate id");
				}
				break;
			case "listtemplates":
				RewardsDAO.getTemplateRewardsAsync().thenAccept(rewards -> {
					if(rewards.isEmpty()) {
						sender.sendMessage("§cIl n'y a pas de templates.");
						return;
					}
					rewards.forEach(r -> sender.sendMessage("§a" + r.id() + " - " + r.name() + " - " + r.server() + " - " + r.command()));
				});
				break;
			case "addtemplate":
				if(args.length >= 4) {
					String name = args[1];
					String server = args[2];
					StringBuilder sb = new StringBuilder(args[3]);
					for(int i = 4 ; i < args.length ; i++) {
						sb.append(" ");
						sb.append(args[i]);
					}
					RewardsDAO.addRewardAsync("template", name, server, sb.toString());
					sender.sendMessage("§aTemplate de récompense ajoutée.");
				}else {
					sender.sendMessage("/recompenses addtemplate name server command ({player} = pseudo du joueur)");
				}
				break;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> complete = new ArrayList<>();
		if(sender.hasPermission("servercore.addrewards")) {
			if(args.length == 1) {
				if("give".startsWith(args[0].toLowerCase())) {
					complete.add("give");
				}
				if("addtemplate".startsWith(args[0].toLowerCase())) {
					complete.add("addtemplate");
				}
				if("removetemplate".startsWith(args[0].toLowerCase())) {
					complete.add("removetemplate");
				}
				if("listtemplates".startsWith(args[0].toLowerCase())) {
					complete.add("listtemplates");
				}
			}
		}
		return complete;
	}

}
