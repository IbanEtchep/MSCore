package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.SLocation;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.manager.TeleportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
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
    @Description("Retour à l'emplacement de votre dernier décès.")
    public void back(Player sender) {
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(sender.getUniqueId());
        SLocation location = profile.getDeathLocation();

        if (location != null) {
            teleportManager.delayedTeleport(sender, location, 3);
        } else {
            sender.sendMessage(Component.text("L'endroit de votre décès n'a pas pu être trouvé.", NamedTextColor.RED));
        }
    }

    @Command("lastrtp")
    @CommandPermission("servercore.lastrtp")
    @Description("Retour à la dernière position de téléportation aléatoire.")
    public void lastRtp(Player sender) {
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(sender.getUniqueId());
        SLocation loc = profile.getLastRTPLocation();

        if (loc != null) {
            plugin.getTeleportManager().delayedTeleport(sender, loc, 2);
        } else {
            sender.sendMessage(Component.text("La position n'a pas été trouvée.", NamedTextColor.RED));
        }
    }
}
