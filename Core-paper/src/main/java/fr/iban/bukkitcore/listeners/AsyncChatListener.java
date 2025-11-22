package fr.iban.bukkitcore.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.PluginMessageHelper;

public class AsyncChatListener implements Listener {

	private final CoreBukkitPlugin plugin;
	private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().extractUrls().build();

	public AsyncChatListener(CoreBukkitPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncChatEvent e) {
		Player player = e.getPlayer();
		String message = legacyComponentSerializer.serialize(e.message());

		if(plugin.getTextInputs().containsKey(player.getUniqueId())) {
			plugin.getScheduler().runAtEntity(
					player,
					task -> plugin.getTextInputs().get(player.getUniqueId()).call(e.message())
			);
			e.setCancelled(true);
			return;
		}

		if(!e.isCancelled() && plugin.getConfig().getBoolean("global-chat", true)) {
			PluginMessageHelper.sendGlobalMessage(player, message);
			e.setCancelled(true);
		}
	}
}