package fr.iban.survivalcore.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidTriggerEvent;

public class RaidTriggerListener implements Listener {

    private final SurvivalCorePlugin plugin;
    private final Map<UUID, Long> cooldown = new HashMap<>();

    public RaidTriggerListener(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent e) {
        final Player player = e.getPlayer();

        if (plugin.getConfig().getBoolean("raids.disable")) {
            player.sendMessage(MessageBuilder.translatable(LangKey.RAIDS_DISABLED).toLegacy());
            e.setCancelled(true);
            return;
        }

        if (player.hasPermission("antiraidfarm.bypass")) {
            return;
        }

        if (isCooldown(player)) {
            e.setCancelled(true);
        } else {
            cooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    private boolean isCooldown(Player player) {
        if (cooldown.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - cooldown.get(player.getUniqueId()) < 300000) {
                return true;
            } else {
                cooldown.remove(player.getUniqueId());
            }
        }
        return false;
    }
}
