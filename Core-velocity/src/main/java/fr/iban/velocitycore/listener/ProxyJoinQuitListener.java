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
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
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
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_1).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_2).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_3).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_4).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_5).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_JOIN_MESSAGE_6).toLegacy()
            };

    private final String[] quitMessages =
            {
                    MessageBuilder.translatable(LangKey.PROXY_QUIT_MESSAGE_1).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_QUIT_MESSAGE_2).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_QUIT_MESSAGE_3).toLegacy(),
                    MessageBuilder.translatable(LangKey.PROXY_QUIT_MESSAGE_4).toLegacy()
            };

    private final String[] longAbsenceMessages = {
            MessageBuilder.translatable(LangKey.PROXY_LONG_ABSENCE_1).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_LONG_ABSENCE_2).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_LONG_ABSENCE_3).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_LONG_ABSENCE_4).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_LONG_ABSENCE_5).toLegacy()
    };

    private final String[] firstJoinMessages = {
            MessageBuilder.translatable(LangKey.PROXY_FIRST_JOIN_1).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_FIRST_JOIN_2).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_FIRST_JOIN_3).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_FIRST_JOIN_4).toLegacy(),
            MessageBuilder.translatable(LangKey.PROXY_FIRST_JOIN_5).toLegacy()
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
                String joinPrefix = MessageBuilder.translatable(LangKey.PROXY_PREFIX_JOIN).toLegacy() + " ";
                String joinMessage;

                if ((System.currentTimeMillis() - lastSeen) > 2592000000L) {
                    joinMessage = joinPrefix + String.format(ArrayUtils.getRandomFromArray(longAbsenceMessages), playerName);
                } else {
                    joinMessage = joinPrefix + String.format(ArrayUtils.getRandomFromArray(joinMessages), playerName);
                }

                Component message = MineDown.parse(joinMessage).hoverEvent(HoverEvent.showText(
                        Component.text(
                                MessageBuilder.translatable(LangKey.PROXY_HOVER_LAST_SEEN)
                                        .placeholder("time", getLastSeen(lastSeen))
                                        .toLegacy(),
                                NamedTextColor.GRAY
                        )
                ));

                proxy.getAllPlayers().forEach(p -> {
                    MSPlayerProfile receiver = playerManager.getProfile(p.getUniqueId());
                    if (receiver.getOption(Option.JOIN_MESSAGE) && !receiver.getIgnoredPlayers().contains(uuid)) {
                        p.sendMessage(message);
                    }
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(message);
            }
        } else {
            String joinPrefix = MessageBuilder.translatable(LangKey.PROXY_PREFIX_FIRST_JOIN).toLegacy() + " ";
            String msg = joinPrefix + String.format(ArrayUtils.getRandomFromArray(firstJoinMessages), playerName);

            Component welcomeComponent = MineDown.parse(msg)
                    .hoverEvent(HoverEvent.showText(
                            MessageBuilder.translatable(LangKey.PROXY_HOVER_FIRST_JOIN).toComponent()
                    ))
                    .clickEvent(ClickEvent.suggestCommand(
                            MessageBuilder.translatable(LangKey.PROXY_CLICK_FIRST_JOIN)
                                    .placeholder("player", playerName)
                                    .toLegacy()
                    ));

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
            String quitPrefix = MessageBuilder.translatable(LangKey.PROXY_PREFIX_QUIT).toLegacy() + " ";
            String quitMessage = quitPrefix + String.format(ArrayUtils.getRandomFromArray(quitMessages), player.getUsername());

            Component quitComponent = MineDown.parse(quitMessage);

            if ((System.currentTimeMillis() - profile.getLastSeen()) > 60000) {
                proxy.getAllPlayers().forEach(p -> {
                    MSPlayerProfile data = playerManager.getProfile(p.getUniqueId());
                    if (data.getOption(Option.LEAVE_MESSAGE) && !data.getIgnoredPlayers().contains(player.getUniqueId())) {
                        p.sendMessage(quitComponent);
                    }
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(quitComponent);
            }

            profile.setLastSeen(System.currentTimeMillis());

            playerManager.saveProfile(profile).join();
            plugin.getPlayerManager().handleProxyQuit(player.getUniqueId());

            plugin.getChatManager().clearPlayerReplies(player);
        });
    }

    private String getLastSeen(long time) {
        if (time == 0) {
            return MessageBuilder.translatable(LangKey.PROXY_LAST_SEEN_NEVER).toLegacy();
        }
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

        if(message.contains(MessageBuilder.translatable(LangKey.PROXY_KICK_KEYWORD).toLegacy())) {
            player.disconnect(serverKickReason);
        }
    }
}
