package fr.iban.velocitycore.command;

import java.util.Map;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;

public class MessageCMD {

    private final CoreVelocityPlugin plugin;

    public MessageCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command({"msg", "message", "m", "w", "tell", "t"})
    @Description("Envoyer un message privé à un joueur.")
    @Usage("/msg <joueur> <message>")
    public void msg(Player player, Player target, String message) {
        plugin.getChatManager().sendMessage(
                player,
                target,
                Lang.get("commands.msg.format", Map.of(
                        "sender", player.getUsername(),
                        "target", target.getUsername(),
                        "message", message
                ))
        );
    }
}
