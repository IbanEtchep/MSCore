package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Usage;

public class ReplyCMD {

    private final CoreVelocityPlugin plugin;

    public ReplyCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command({"reply", "r"})
    @Usage("/reply <message>")
    public void reply(Player player, @Default("") @Named("message") String message) {
        if (message.isEmpty()) {
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.REPLY_USAGE).toComponent()
            );
            return;
        }

        Player target = plugin.getChatManager().getPlayerToReply(player);

        if (target != null) {
            plugin.getChatManager().sendMessage(player, target, message.trim());
        } else {
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.REPLY_NO_TARGET).toComponent()
            );
        }
    }
}
