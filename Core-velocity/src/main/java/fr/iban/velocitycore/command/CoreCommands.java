package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

public class CoreCommands {

    private final CoreVelocityPlugin plugin;

    public CoreCommands(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("site")
    public void siteCommand(VelocityCommandActor actor) {
        sendMessage(actor, "messages.site");
    }

    @Command("discord")
    public void discordCommand(VelocityCommandActor actor) {
        sendMessage(actor, "messages.discord");
    }

    @Command("vote")
    public void voteCommand(VelocityCommandActor actor) {
        sendMessage(actor, "messages.vote");
    }

    @Command("grades")
    public void gradesCommand(VelocityCommandActor actor) {
        sendMessage(actor, "messages.grades");
    }

    @Command("wiki")
    public void wikiCommand(VelocityCommandActor actor) {
        sendMessage(actor, "messages.wiki");
    }

    private void sendMessage(VelocityCommandActor actor, String configPath) {
        String message = plugin.getConfig().getString(configPath);

        if (message == null || message.isBlank()) {
            plugin.getLogger().warn("Clé de configuration manquante ou vide : {}", configPath);
            return;
        }

        Component component = MiniMessage.miniMessage().deserialize(message);

        if (actor instanceof Player player) {
            player.sendMessage(component);
        } else {
            actor.reply(component);
        }
    }
}
