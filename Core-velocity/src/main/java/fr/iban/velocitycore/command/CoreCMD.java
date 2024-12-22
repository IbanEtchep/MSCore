package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.messaging.AbstractMessenger;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.*;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;

import java.io.IOException;

@Command("vcore")
@CommandPermission("servercore.admin")
public class CoreCMD {

    private final CoreVelocityPlugin plugin;

    public CoreCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("help")
    @CommandPlaceholder
    @Description("Affiche les options de commande pour le serveur.")
    public void core(VelocityCommandActor actor) {
        Component message = Component.text("Utilisez ", NamedTextColor.GRAY)
                .append(Component.text("/vcore reload", NamedTextColor.GREEN))
                .append(Component.text(" pour recharger la configuration.\n", NamedTextColor.GRAY))
                .append(Component.text("/vcore debug", NamedTextColor.GREEN))
                .append(Component.text(" pour basculer le mode débogage.", NamedTextColor.GRAY));
        actor.reply(message);
    }

    @Subcommand("reload")
    @Description("Recharge la configuration du serveur.")
    @Usage("/vcore reload")
    public void reloadConfig(VelocityCommandActor actor) throws IOException {
        plugin.getConfig().reload();
        plugin.getAnnounceManager().reloadAnnounces();
        actor.reply(Component.text("La configuration a été rechargée avec succès.", NamedTextColor.GREEN));
    }

    @Subcommand("debug")
    @Description("Active ou désactive le mode débogage.")
    @Usage("/vcore debug")
    public void toggleDebug(Player player) {
        AbstractMessenger messenger = plugin.getMessagingManager().getMessenger();
        messenger.setDebugMode(!messenger.isDebugMode());
        player.sendMessage(Component.text("Mode débogage : " + (messenger.isDebugMode() ? "activé" : "désactivé"), NamedTextColor.YELLOW));
    }
}
