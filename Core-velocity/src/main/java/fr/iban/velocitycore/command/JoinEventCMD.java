package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.*;

public class JoinEventCMD {

    private final CoreVelocityPlugin plugin;

    public JoinEventCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("joinevent")
    @Description("Rejoignez un événement en cours ou spécifique si un nom est fourni.")
    @Usage("/joinevent <event>")
    public void joinEvent(Player player, @Optional @Named("event") String eventName) {
        if (eventName == null) {
            handleJoinEvent(player, plugin.getCurrentEvents().lastKey());
        } else {
            handleJoinEvent(player, eventName);
        }
    }

    private void handleJoinEvent(Player player, String event) {
        if (plugin.getCurrentEvents().containsKey(event)) {
            plugin.getTeleportManager().delayedTeleport(player, plugin.getCurrentEvents().get(event), 3);
        } else {
            player.sendMessage(Component.text(Lang.get("joinevent.not-found"), NamedTextColor.RED));
        }
    }
}
