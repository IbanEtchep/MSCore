package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.messaging.message.PlayerSLocationMessage;
import fr.iban.common.model.MSPlayerProfile;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportPostEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RTPListeners implements Listener {

    @EventHandler
    public void onRTP(RTP_TeleportPostEvent e) {
        Player player = e.getPlayer();
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        PlayerManager playerManager = core.getPlayerManager();

        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());
        profile.setLastRTPLocation(SLocationUtils.getSLocation(player.getLocation()));
        playerManager.saveProfile(profile);

        if (core.getServerManager().isSurvivalServer()) {
            String server = core.getServerName();
            player.sendMessage("§6ⓘ §fVous avez été téléporté aléatoirement sur le serveur §8" + server + "§f. " +
                    "Pensez à mettre une résidence avec la commande §8/sethome§f ou à dormir dans un lit afin de pouvoir retourner à cet endroit plus tard.");
        }
    }

}
