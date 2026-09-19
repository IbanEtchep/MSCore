package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.menu.ServeurMenu;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;


public class ServerMenuCommand {

    @Command("serveur")
    public void serveurMenu(Player sender) {
        new ServeurMenu(sender).open();
    }

}
