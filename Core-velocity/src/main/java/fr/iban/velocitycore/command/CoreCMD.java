package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.messaging.AbstractMessenger;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
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

    @CommandPlaceholder
    public void core(VelocityCommandActor actor) {
        help(actor);
    }

    @Subcommand("help")
    @Description("Affiche les options de commande pour le serveur.")
    public void help(VelocityCommandActor actor) {
        Component message = Component.text(Lang.get("vcore.help"), NamedTextColor.GRAY);
        actor.reply(message);
    }

    @Subcommand("reload")
    @Description("Recharge la configuration du serveur.")
    @Usage("/vcore reload")
    public void reloadConfig(VelocityCommandActor actor) throws IOException {
        plugin.getConfig().reload();
        plugin.getAnnounceManager().reloadAnnounces();
        actor.reply(Component.text(Lang.get("vcore.reload-success"), NamedTextColor.GREEN));
    }

    @Subcommand("debug")
    @Description("Active ou désactive le mode débogage.")
    @Usage("/vcore debug")
    public void toggleDebug(Player player) {
        AbstractMessenger messenger = plugin.getMessagingManager().getMessenger();
        messenger.setDebugMode(!messenger.isDebugMode());
        player.sendMessage(Component.text(
                Lang.get("vcore.debug", 
                        java.util.Map.of("state", messenger.isDebugMode() ? Lang.get("vcore.enabled") : Lang.get("vcore.disabled"))
                ),
                NamedTextColor.YELLOW
        ));
    }
}
