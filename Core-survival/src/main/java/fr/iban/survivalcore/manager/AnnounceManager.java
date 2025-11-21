package fr.iban.survivalcore.manager;

import com.earth2me.essentials.utils.DateUtil;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.MessagingManager;
import fr.iban.survivalcore.utils.Lang;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.survivalcore.SurvivalCorePlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class AnnounceManager {

    private final SurvivalCorePlugin plugin;

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_TIME = 3600;

    public AnnounceManager(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void sendAnnounce(Player player, String message) {
        UUID uuid = player.getUniqueId();

        if (hasCooldown(player)) {
            return;
        }

        if (plugin.getEconomy().getBalance(player) >= 250) {
            plugin.getEconomy().withdrawPlayer(player, 250);
            PluginMessageHelper.sendAnnonce(player, message);
            cooldowns.put(uuid, System.currentTimeMillis());
            MessagingManager messagingManager = CoreBukkitPlugin.getInstance().getMessagingManager();
            messagingManager.sendMessage(CoreChannel.SYNC_ANNOUNCE_COOLDOWN_CHANNEL, uuid.toString());
        } else {
            player.sendMessage(Lang.get("announce.not-enough-money"));
        }
    }


    public boolean hasCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastUsage = cooldowns.get(player.getUniqueId());

            long secondsLeft = ((lastUsage / 1000) + COOLDOWN_TIME) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                player.sendMessage(Lang.get("announce.cooldown")
                        .replace("%time%", DateUtil.formatDateDiff(lastUsage + COOLDOWN_TIME * 1000)));

                return true;
            }

            cooldowns.remove(player.getUniqueId());
        }

        return false;
    }


    public HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
