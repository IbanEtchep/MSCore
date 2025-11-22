package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
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

    @CommandPlaceholder
    public void chat(Player player) {
        help(player);
    }

    @Subcommand("help")
    public void help(Player player) {
        player.sendMessage(MessageBuilder.translatable(LangKey.CHAT_HELP_TITLE).toComponent());
        player.sendMessage(MessageBuilder.translatable(LangKey.CHAT_HELP_TOGGLE).toComponent());
    }

    @Subcommand("clear")
    public void clearChat(Player player) {
        Component emptyMessage = Component.empty();
        server.getAllPlayers().forEach(p -> {
            for (int i = 0; i < 200; i++) {
                p.sendMessage(emptyMessage);
            }
        });

        server.getAllPlayers().forEach(p ->
                p.sendMessage(
                        MessageBuilder
                                .translatable(LangKey.CHAT_CLEAR)
                                .placeholder("player", player.getUsername())
                                .toComponent()
                )
        );
    }

    @Subcommand("toggle")
    public void toggleChat(Player player) {
        plugin.getChatManager().toggleChat(player);
    }
}
