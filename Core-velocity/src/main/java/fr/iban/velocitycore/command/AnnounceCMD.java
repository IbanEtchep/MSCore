package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.manager.AutomatedAnnounceManager;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

@Command("announce")
@Description("Commandes pour gérer les annonces.")
public class AnnounceCMD {

    private final CoreVelocityPlugin plugin;

    public AnnounceCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("listdisabled")
    @Description("Liste toutes les annonces désactivées par l'utilisateur.")
    public void listDisabledAnnouncements(VelocityCommandActor actor, Player player) {
        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

        for (int idA : profile.getBlackListedAnnounces()) {
            player.sendMessage(Component.text("- " + idA));
        }
    }

    @Subcommand("disable")
    @Description("Désactive une annonce spécifique pour l'utilisateur.")
    public void disableAnnouncement(Player player, @Default("0") int id) {
        AutomatedAnnounceManager announceManager = plugin.getAnnounceManager();
        if (announceManager.getAnnounces().containsKey(id)) {
            MSPlayerProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

            if (!profile.getBlackListedAnnounces().contains(id)) {
                profile.getBlackListedAnnounces().add(id);
                player.sendMessage(Component.text(Lang.get("announce.disabled")).color(NamedTextColor.GREEN));
                plugin.getPlayerManager().saveProfile(profile);
            } else {
                player.sendMessage(Component.text(Lang.get("announce.already-disabled")).color(NamedTextColor.RED));
            }
        } else {
            player.sendMessage(Component.text(Lang.get("announce.not-exist")).color(NamedTextColor.RED));
        }
    }
}
