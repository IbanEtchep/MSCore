package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import revxrsal.commands.annotation.Command;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;

public class MiscellaneousCommands {

    @Command("miscellaneous")
    public void miscellaneous(Player player) {
        if (player.getUsername().startsWith(".")) {
            player.sendMessage(MessageBuilder.translatable(LangKey.MISC_GD_OFFHAND).toComponent());
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.MISC_NOT_BEDROCK).toComponent());
        }
    }
}
