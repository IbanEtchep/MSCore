package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.util.Optional;

public class MessageCMD {

    private final CoreVelocityPlugin plugin;

    public MessageCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command({"msg", "message", "m", "w", "tell", "t"})
    @Description("Envoyer un message privé à un joueur.")
    @Usage("/msg <target> <message>")
    public void msg(VelocityCommandActor sender, MSPlayerProfile target, String message) {
        Player player = sender.requirePlayer();
        Optional<Player> targetPlayer = plugin.getServer().getPlayer(target.getUniqueId());
        targetPlayer.ifPresent(value -> plugin.getChatManager().sendMessage(player, value, message));
    }
}
