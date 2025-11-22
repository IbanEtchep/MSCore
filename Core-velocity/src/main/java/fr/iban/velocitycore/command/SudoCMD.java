package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class SudoCMD {

    private final ProxyServer server;

    public SudoCMD(CoreVelocityPlugin plugin) {
        this.server = plugin.getServer();
    }

    @Command("sudo")
    @CommandPermission("servercore.sudo")
    @Usage("/sudo <player> <message|command>")
    public void execute(VelocityCommandActor sender, Player target, String message) {
        if (message.startsWith("/")) {
            server.getCommandManager().executeAsync(target, message.substring(1));
        } else {
            target.spoofChatInput(message);
        }
    }
}
