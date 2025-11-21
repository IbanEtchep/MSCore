package fr.iban.velocitycore.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.themoep.minedown.adventure.MineDown;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.utils.ArrayUtils;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ProxyJoinQuitListener {

    private final CoreVelocityPlugin plugin;
    private final PlayerManager playerManager;

    private final String[] joinMessages =
            {
                    Lang.get("proxy.join-messages.1"),
                    Lang.get("proxy.join-messages.2"),
                    Lang.get("proxy.join-messages.3"),
                    Lang.get("proxy.join-messages.4"),
                    Lang.get("proxy.join-messages.5"),
                    Lang.get("proxy.join-messages.6")
            };

    private final String[] quitMessages =
            {
                    Lang.get("proxy.quit-messages.1"),
                    Lang.get("proxy.quit-messages.2"),
                    Lang.get("proxy.quit-messages.3"),
                    Lang.get("proxy.quit-messages.4")
            };

    private final String[] longAbsenceMessages = {
            Lang.get("proxy.long-absence.1"),
            Lang.get("proxy.long-absence.2"),
            Lang.get("proxy.long-absence.3"),
            Lang.get("proxy.long-absence.4"),
            Lang.get("proxy.long-absence.5")
    };

    private final String[] firstJoinMessages = {
            Lang.get("proxy.first-join.1"),
            Lang.get("proxy.first-join.2"),
            Lang.get("proxy.first-join.3"),
            Lang.get("proxy.first-join.4"),
            Lang.get("proxy.first-join.5")
    };

    public ProxyJoinQuitListener(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }


    @Subscribe
    public void onProxyJoin(ServerConnectedEvent e) {
        if(e.getPreviousServer().isPresent()) {
            return;
        }

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getUsername();
        ProxyServer proxy = plugin.getServer();
        MSPlayerProfile profile = playerManager.loadProfile(uuid, playerName);

        long lastSeen = profile.getLastSeen();
        if (lastSeen != 0) {
            if ((System.currentTimeMillis() - lastSeen) > 60000) {
                String joinMessage = Lang.get("proxy.prefix.join") + " ";
                if ((System.currentTimeMillis() - lastSeen) > 2592000000L) {
                    joinMessage += String.format(ArrayUtils.getRandomFromArray(longAbsenceMessages), playerName);
                } else {
                    joinMessage += String.format(ArrayUtils.getRandomFromArray(joinMessages), playerName);
                }

                Component message = MineDown.parse(joinMessage).hoverEvent(HoverEvent.showText(
                        Component.text(Lang.get("proxy.hover.last-seen").replace("%time%", getLastSeen(lastSeen)), NamedTextColor.GRAY)
                ));

                proxy.getAllPlayers().forEach(p -> {
                    MSPlayerProfile receiverAccount = playerManager.getProfile(p.getUniqueId());
                    if (receiverAccount.getOption(Option.JOIN_MESSAGE) && !receiverAccount.getIgnoredPlayers().contains(uuid)) {
                        p.sendMessage(message);
                    }
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(message);
            }
        } else {
            String firstJoinMessage = Lang.get("proxy.prefix.first-join") + " " + String.format(ArrayUtils.getRandomFromArray(firstJoinMessages), playerName);
            Component welcomeComponent = MineDown.parse(firstJoinMessage)
                    .hoverEvent(HoverEvent.showText(Component.text(Lang.get("proxy.hover.first-join"))))
                    .clickEvent(ClickEvent.suggestCommand(Lang.get("proxy.click.first-join").replace("%player%", playerName)));

            proxy.sendMessage(welcomeComponent);
        }

        profile.setName(playerName);
        profile.setIp(player.getRemoteAddress().getHostString());
        profile.setLastSeen(System.currentTimeMillis());

        playerManager.saveProfile(profile)
                .thenRun(() -> plugin.getPlayerManager().handleProxyJoin(profile))
                .exceptionally(e1 -> {
                    e1.printStackTrace();
                    return null;
                });
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent e) {
        Player player = e.getPlayer();
        ProxyServer proxy = plugin.getServer();
        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

        if(profile == null) return null;

        return EventTask.async(() -> {
            String quitMessage = Lang.get("proxy.prefix.quit") + " " + String.format(ArrayUtils.getRandomFromArray(quitMessages), player.getUsername());
            Component quitMessageComponent = MineDown.parse(quitMessage);

            if ((System.currentTimeMillis() - profile.getLastSeen()) > 60000) {
                proxy.getAllPlayers().forEach(p -> {
                    MSPlayerProfile account2 = playerManager.getProfile(p.getUniqueId());
                    if (account2.getOption(Option.LEAVE_MESSAGE) && !account2.getIgnoredPlayers().contains(player.getUniqueId())) {
                        p.sendMessage(quitMessageComponent);
                    }
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(quitMessageComponent);
            }

            profile.setLastSeen(System.currentTimeMillis());

            playerManager.saveProfile(profile).join();
            plugin.getPlayerManager().handleProxyQuit(player.getUniqueId());

            plugin.getChatManager().clearPlayerReplies(player);
        });
    }

    private String getLastSeen(long time) {
        if (time == 0) return Lang.get("proxy.last-seen.never");
        PrettyTime prettyTime = new PrettyTime(Locale.FRANCE);
        return prettyTime.format(new Date(time));
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        Component serverKickReason = event.getServerKickReason().orElse(null);

        if(serverKickReason == null) {
            return;
        }

        String message = PlainTextComponentSerializer.plainText().serialize(serverKickReason);

        if(message.contains(Lang.get("proxy.kick.keyword"))) {
            player.disconnect(serverKickReason);
        }
    }
}
