package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.SLocation;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.manager.TeleportManager;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class TeleportCommands {

    private final CoreVelocityPlugin plugin;
    private final TeleportManager teleportManager;

    public TeleportCommands(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Command("back")
    @CommandPermission("servercore.back.death")
    public void back(Player sender) {
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(sender.getUniqueId());
        SLocation location = profile.getDeathLocation();

        if (location != null) {
            teleportManager.delayedTeleport(sender, location, 3);
        } else {
            sender.sendMessage(MessageBuilder.translatable(LangKey.TELEPORT_BACK_NOT_FOUND).toComponent());
        }
    }

    @Command("lastrtp")
    @CommandPermission("servercore.lastrtp")
    public void lastRtp(Player sender) {
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(sender.getUniqueId());
        SLocation loc = profile.getLastRTPLocation();

        if (loc != null) {
            plugin.getTeleportManager().delayedTeleport(sender, loc, 2);
        } else {
            sender.sendMessage(MessageBuilder.translatable(LangKey.TELEPORT_LASTRTP_NOT_FOUND).toComponent());
        }
    }
}
