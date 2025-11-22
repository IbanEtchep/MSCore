package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;

public class MessageCMD {

    private final CoreVelocityPlugin plugin;

    public MessageCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command({"msg", "message", "m", "w", "tell", "t"})
    @Usage("/msg <player> <message>")
    public void msg(Player player, Player target, String message) {
        plugin.getChatManager().sendMessage(
                player,
                target,
                MessageBuilder.translatable(LangKey.COMMANDS_MSG_FORMAT)
                        .placeholder("sender", player.getUsername())
                        .placeholder("target", target.getUsername())
                        .placeholder("message", message)
                        .toLegacy()
        );
    }
}
