package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.velocity.annotation.CommandPermission;

@Command("chat")
@CommandPermission("servercore.chatmanage")
public class ChatCMD {

    private final CoreVelocityPlugin plugin;
    private final ProxyServer server;

    public ChatCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Subcommand("help")
    @CommandPlaceholder
    @Description("Affiche les commandes de gestion du chat.")
    public void chat(Player player) {
        player.sendMessage(Component.text("Commandes chat :", NamedTextColor.GREEN));
        player.sendMessage(Component.text("/chat toggle - mute/unmute le chat", NamedTextColor.GREEN));
    }

    @Subcommand("clear")
    @Description("Efface le chat pour tous les utilisateurs.")
    public void clearChat(Player player) {
        Component emptyMessage = Component.empty();
        server.getAllPlayers().forEach(p -> {
            for (int i = 0; i < 200; i++) {
                p.sendMessage(emptyMessage);
            }
        });
        server.getAllPlayers().forEach(p -> p.sendMessage(Component.text("Le chat a été clear par " + player.getUsername() + ".", NamedTextColor.RED)));
    }

    @Subcommand("toggle")
    @Description("Active ou désactive le chat.")
    public void toggleChat(Player player) {
        plugin.getChatManager().toggleChat(player);
    }
}
