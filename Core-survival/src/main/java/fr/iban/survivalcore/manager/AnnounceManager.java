package fr.iban.survivalcore.manager;

import com.earth2me.essentials.utils.DateUtil;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.MessagingManager;
import fr.iban.bukkitcore.utils.ChatItemHelper;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.common.chat.ChatItemTokens;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.survivalcore.SurvivalCorePlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AnnounceManager {

    private final SurvivalCorePlugin plugin;

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

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
            String itemJson = ChatItemHelper.captureHeldItemJson(player, message);
            if (itemJson != null) {
                PluginMessageHelper.sendAnnonceItem(player, ChatItemTokens.replaceWithSentinel(message), itemJson);
            } else {
                PluginMessageHelper.sendAnnonce(player, message);
            }
            cooldowns.put(uuid, System.currentTimeMillis());
            MessagingManager messagingManager = CoreBukkitPlugin.getInstance().getMessagingManager();
            messagingManager.sendMessage(CoreChannel.SYNC_ANNOUNCE_COOLDOWN_CHANNEL, uuid.toString());
        } else {
            player.sendMessage("§cIl vous faut 250$ pour faire une annonce !");
        }
    }


    public boolean hasCooldown(Player player) {
        if (player.hasPermission("servercore.annonce.bypasscooldown")) {
            return false;
        }
        if (cooldowns.containsKey(player.getUniqueId())) {
            int cooldownTime = getCooldownFromConfig(player, "annonce") * 1000;
            long lastUsage = cooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() - lastUsage > cooldownTime) {
                cooldowns.remove(player.getUniqueId());
                return false;
            } else {
                player.sendMessage("§cVous pourrez à nouveau faire ça dans " + DateUtil.formatDateDiff(lastUsage + cooldownTime) + ".");
                return true;
            }
        }
        return false;
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


    public HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
