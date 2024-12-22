package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;

public class MiscellaneousCommands {

    @Command("miscellaneous")
    public void miscellaneous(Player player) {
        if (player.getUsername().startsWith(".")) {
            player.sendMessage(Component.text("/geyser offhand", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Commande réservée aux utilisateurs de la version bedrock.", NamedTextColor.RED));
        }
    }
}
