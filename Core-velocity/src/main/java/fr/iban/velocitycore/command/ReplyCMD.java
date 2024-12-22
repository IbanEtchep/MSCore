package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.*;

public class ReplyCMD {

    private final CoreVelocityPlugin plugin;

    public ReplyCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command({"reply", "r"})
    @Description("Répondre au dernier joueur qui vous a contacté.")
    @Usage("/reply <message> - Répond au dernier message reçu.")
    public void reply(Player player, @Default("") @Named("message") String message) {
        if (message.isEmpty()) {
            player.sendMessage(Component.text("Utilisation: /r <message>").color(NamedTextColor.YELLOW));
            return;
        }

        Player target = plugin.getChatManager().getPlayerToReply(player);

        if (target != null) {
            plugin.getChatManager().sendMessage(player, target, message.trim());
        } else {
            player.sendMessage(Component.text("Tu ne peux pas répondre, car personne ne t'a écrit.", NamedTextColor.RED));
        }
    }
}