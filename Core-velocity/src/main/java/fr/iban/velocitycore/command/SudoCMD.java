package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class SudoCMD {

    private final ProxyServer server;

    public SudoCMD(CoreVelocityPlugin plugin) {
        this.server = plugin.getServer();
    }

    @Command("sudo")
    @Description("Permet à un joueur de faire parler un autre joueur ou d'exécuter une commande en son nom.")
    @CommandPermission("servercore.sudo")
    @Usage("/sudo <joueur> <message|commande>")
    public void execute(VelocityCommandActor sender, Player target, String message) {
        if (message.startsWith("/")) {
            server.getCommandManager().executeAsync(target, message.substring(1));
        } else {
            target.spoofChatInput(message);
        }
    }
}
