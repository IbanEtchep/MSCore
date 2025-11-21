package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
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
    @Description("Annonce un événement à tous les joueurs.")
    @Usage("/announceevent <message>")
    public void announceEvent(Player player, @Optional @Single String message) {
        if (message == null || message.isEmpty()) {
            player.sendMessage(Component.text(Lang.get("announceevent.usage"), NamedTextColor.RED));
            return;
        }

        Component broadcastMessage = Component.text(Lang.get("announceevent.prefix"), NamedTextColor.GOLD)
                .append(Component.text(message.trim(), NamedTextColor.WHITE))
                .decoration(TextDecoration.BOLD, true);

        server.getAllPlayers().forEach(p -> p.sendMessage(broadcastMessage));
    }
}
