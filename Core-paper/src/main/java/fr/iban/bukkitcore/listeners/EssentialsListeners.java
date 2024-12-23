package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.message.PlayerStringMessage;
import fr.iban.common.model.MSPlayerProfile;
import net.ess3.api.IUser;
import net.ess3.api.events.KitClaimEvent;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class EssentialsListeners implements Listener {

    private final CoreBukkitPlugin plugin;

    public EssentialsListeners(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVanish(VanishStatusChangeEvent event) {
        UUID uniqueId = event.getAffected().getBase().getUniqueId();
        MSPlayerProfile msPlayer = plugin.getPlayerManager().getProfile(uniqueId);
        msPlayer.setVanished(event.getValue());
        plugin.getPlayerManager().saveProfile(msPlayer);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        IUser user = plugin.getEssentials().getUser(player);
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if(profile != null && profile.isVanished() && !user.isVanished()) {
            user.setVanished(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKitClaim(KitClaimEvent e) {
        if(e.isCancelled()) {
            return;
        }

        UUID uuid = e.getUser().getBase().getUniqueId();
        String kitName = e.getKit().getName();

        plugin.getMessagingManager().sendMessage(CoreChannel.SYNC_KIT_CLAIM, new PlayerStringMessage(uuid, kitName));
    }

}
