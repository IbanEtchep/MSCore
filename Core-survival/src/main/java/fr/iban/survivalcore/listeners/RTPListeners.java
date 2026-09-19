package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.survivalcore.SurvivalCorePlugin;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportPostEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class RTPListeners implements Listener {

    private final SurvivalCorePlugin plugin;

    public RTPListeners(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRTP(RTP_TeleportPostEvent e) {
        Player player = e.getPlayer();
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        PlayerManager playerManager = core.getPlayerManager();

        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());
        profile.setLastRTPLocation(SLocationUtils.getSLocation(player.getLocation()));
        playerManager.saveProfile(profile);

        List<String> resourceWorlds = plugin.getConfig().getStringList("resource-worlds");
        if (resourceWorlds.contains(player.getWorld().getName())) {
            return;
        }

        if (core.getServerManager().isSurvivalServer()) {
            player.sendMessage(plugin.getLangManager().get("rtp.survival-teleport"));
        }
    }

}
