package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import revxrsal.commands.annotation.*;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class AnnounceEventCMD {

    private final ProxyServer server;

    public AnnounceEventCMD(CoreVelocityPlugin plugin) {
        this.server = plugin.getServer();
    }

    @Command("announceevent")
    @CommandPermission("servercore.announceevent")
    @Usage("/announceevent <message>")
    public void announceEvent(Player player, @Optional @Single String message) {
        if (message == null || message.isEmpty()) {
            player.sendMessage(MessageBuilder.translatable(LangKey.ANNOUNCEEVENT_USAGE).toComponent());
            return;
        }

        Component broadcastMessage =
                MessageBuilder.translatable(LangKey.ANNOUNCEEVENT_PREFIX).toComponent()
                        .append(Component.text(message.trim(), NamedTextColor.WHITE))
                        .decoration(TextDecoration.BOLD, true);

        server.getAllPlayers().forEach(p -> p.sendMessage(broadcastMessage));
    }
}
