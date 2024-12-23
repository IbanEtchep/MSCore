package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionBarCMD {
	
	private final List<String> queue = new ArrayList<>();
	private final CoreBukkitPlugin plugin;

	public ActionBarCMD(CoreBukkitPlugin plugin) {
		this.plugin = plugin;
	}

	@Command("abbc")
	@CommandPermission("servercore.abbc")
	public void broadcastActionBar(BukkitCommandActor actor, String message) {
		if(queue.isEmpty()) {
			sendActionBar(message);
		}

		queue.add(message);
	}
	
	private void sendActionBar(String message) {
		AtomicInteger time = new AtomicInteger();
		plugin.getScheduler().runTimer(task -> {
			Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(ChatUtils.translateColors(message)));
			time.getAndIncrement();

			if(time.get() == 4) {
				queue.removeFirst();
				if(!queue.isEmpty()) {
					sendActionBar(queue.getFirst());
				}

				task.cancel();
			}
		}, 0, 20L);
	}
}