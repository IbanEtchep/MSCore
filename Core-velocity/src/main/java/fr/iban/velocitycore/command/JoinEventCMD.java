package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.annotation.Named;

public class JoinEventCMD {

    private final CoreVelocityPlugin plugin;

    public JoinEventCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("joinevent")
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
            player.sendMessage(MessageBuilder.translatable(LangKey.JOINEVENT_NOT_FOUND).toComponent());
        }
    }
}
