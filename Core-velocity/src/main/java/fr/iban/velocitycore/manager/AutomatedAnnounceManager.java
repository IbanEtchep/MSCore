package fr.iban.velocitycore.manager;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.velocitypowered.api.proxy.Player;
import fr.iban.common.utils.ArrayUtils;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AutomatedAnnounceManager {

	private final CoreVelocityPlugin plugin;
	private final Map<Integer, Component> announces = new HashMap<>();
	private final MiniMessage miniMessage = MiniMessage.miniMessage();

	public AutomatedAnnounceManager(CoreVelocityPlugin plugin) {
		this.plugin = plugin;
		loadAnnounces();
		startAnnounceTask();
	}

	public Map<Integer, Component> getAnnounces() {
		return announces;
	}

	private Component getMessage(int id, String msg) {
		Component croix = Component.text(" [")
				.append(Component.text("✕", NamedTextColor.RED))
				.append(Component.text("]", NamedTextColor.GRAY))
				.hoverEvent(HoverEvent.showText(Component.text("Clic pour ne plus afficher cette annonce", NamedTextColor.RED)))
				.clickEvent(ClickEvent.runCommand("/announce disable " + id));

		return miniMessage.deserialize(msg).append(croix);
	}

	private void loadAnnounces() {
		Section messagesSection = plugin.getConfig().getSection("announces.messages");

		if (messagesSection == null) {
			plugin.getLogger().warn("No announces found in config!");
			return;
		}

		for (String key : messagesSection.getRoutesAsStrings(false)) {
			try {
				int id = Integer.parseInt(key);
				String message = messagesSection.getString(key + ".message");

				if (message != null) {
					announces.put(id, getMessage(id, message));
				}
			} catch (NumberFormatException e) {
				plugin.getLogger().error("Invalid announce ID: " + key);
			}
		}
	}

	private void startAnnounceTask() {
		int interval = plugin.getConfig().getInt("announces.interval", 300);

		plugin.getServer().getScheduler().buildTask(plugin, () -> {
			if (announces.isEmpty()) return;

			int id = (int) ArrayUtils.getRandomFromArray(announces.keySet().toArray());
			Component announce = announces.get(id);

			for (Player player : plugin.getServer().getAllPlayers()) {
				if (!plugin.getAccountManager().getAccount(player.getUniqueId()).getBlackListedAnnounces().contains(id)) {
					player.sendMessage(announce);
				}
			}
		}).repeat(interval, TimeUnit.SECONDS).schedule();
	}

	public void reloadAnnounces() {
		announces.clear();
		loadAnnounces();
	}
}