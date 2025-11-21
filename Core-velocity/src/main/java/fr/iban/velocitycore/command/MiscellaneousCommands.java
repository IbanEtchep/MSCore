package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import fr.iban.velocitycore.util.Lang;

public class MiscellaneousCommands {

    @Command("miscellaneous")
    public void miscellaneous(Player player) {
        if (player.getUsername().startsWith(".")) {
            player.sendMessage(Component.text(Lang.get("miscellaneous.geyser-offhand"), NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(Lang.get("miscellaneous.not-bedrock"), NamedTextColor.RED));
        }
    }
}
