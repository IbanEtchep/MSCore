package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.manager.AutomatedAnnounceManager;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

@Command("announce")
public class AnnounceCMD {

    private final CoreVelocityPlugin plugin;

    public AnnounceCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("listdisabled")
    public void listDisabledAnnouncements(VelocityCommandActor actor, Player player) {
        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

        for (int idA : profile.getBlackListedAnnounces()) {
            player.sendMessage(Component.text("- " + idA));
        }
    }

    @Subcommand("disable")
    public void disableAnnouncement(Player player, @Default("0") int id) {
        AutomatedAnnounceManager announceManager = plugin.getAnnounceManager();
        if (announceManager.getAnnounces().containsKey(id)) {
            MSPlayerProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

            if (!profile.getBlackListedAnnounces().contains(id)) {
                profile.getBlackListedAnnounces().add(id);
                player.sendMessage(MessageBuilder.translatable(LangKey.ANNOUNCE_DISABLED).toComponent());
                plugin.getPlayerManager().saveProfile(profile);
            } else {
                player.sendMessage(MessageBuilder.translatable(LangKey.ANNOUNCE_ALREADY_DISABLED).toComponent());
            }
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.ANNOUNCE_NOT_EXIST).toComponent());
        }
    }
}
