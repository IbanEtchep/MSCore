package fr.iban.velocitycore.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.utils.ArrayUtils;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ProxyJoinQuitListener {

    private final CoreVelocityPlugin plugin;
    private final PlayerManager playerManager;

    private static final String JOIN_PREFIX = "<dark_gray>[<green>+</green>]</dark_gray> <dark_gray>";
    private static final String QUIT_PREFIX = "<dark_gray>[<red>-</red>]</dark_gray> <dark_gray>";
    private static final String FIRST_JOIN_PREFIX = "<dark_gray>≫</dark_gray> <gray>";

    private final String[] joinMessages =
            {
                    "<player> s'est connecté !",
                    "<player> est dans la place !",
                    "<player> a rejoint le serveur !",
                    "Un <player> sauvage apparaît ! ",
                    "Tout le monde, dites bonjour à <player> !",
                    "<player> vient d'arriver, faites place !"
            };

    private final String[] quitMessages =
            {
                    "<player> nous a quitté :(",
                    "<player> s'est déconnecté.",
                    "<player> a disparu dans l'ombre.",
                    "Et voilà, <player> est parti."
            };

    private final String[] longAbsenceMessages = {
            "<player> est enfin de retour !",
            "Oh mon dieu, <player> est revenu !",
            "Le prodige <player> est de retour !",
            "<player>, notre légende perdue est enfin de retour !",
            "Vous vous rappelez de <player> ? Eh bien, devinez qui vient de revenir !"
    };

    private final String[] firstJoinMessages = {
            "Bienvenue <player>, content de te voir pour la première fois !",
            "<player> vient de franchir les portes du serveur pour la première fois !",
            "C'est la grande première de <player> sur le serveur !",
            "Hey tout le monde, accueillons notre nouveau membre, <player> !",
            "Un nouveau visage ! Salut <player>, bienvenue sur le serveur !"
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
                String[] pool = (System.currentTimeMillis() - lastSeen) > 2592000000L ? longAbsenceMessages : joinMessages;
                String joinMessage = JOIN_PREFIX + ArrayUtils.getRandomFromArray(pool);

                Component message = deserialize(joinMessage, playerName).hoverEvent(HoverEvent.showText(
                        Component.text("Vu pour la dernière fois " + getLastSeen(lastSeen), NamedTextColor.GRAY)
                ));

                playerManager.getProfiles().forEach(receiverAccount -> {
                    proxy.getPlayer(receiverAccount.getUniqueId()).ifPresent(p -> {
                        if (receiverAccount.getOption(Option.JOIN_MESSAGE) && !receiverAccount.getIgnoredPlayers().contains(uuid)) {
                            p.sendMessage(message);
                        }
                    });
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(message);
            }
        } else {
            String firstJoinMessage = FIRST_JOIN_PREFIX + ArrayUtils.getRandomFromArray(firstJoinMessages);
            Component welcomeComponent = deserialize(firstJoinMessage, playerName)
                    .hoverEvent(HoverEvent.showText(Component.text("Clic !")))
                    .clickEvent(ClickEvent.suggestCommand(" Bienvenue " + playerName));

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
            String quitMessage = QUIT_PREFIX + ArrayUtils.getRandomFromArray(quitMessages);
            Component quitMessageComponent = deserialize(quitMessage, player.getUsername());

            if ((System.currentTimeMillis() - profile.getLastSeen()) > 60000) {
                playerManager.getProfiles().forEach(account2 -> {
                    proxy.getPlayer(account2.getUniqueId()).ifPresent(p -> {
                        if (account2.getOption(Option.LEAVE_MESSAGE) && !account2.getIgnoredPlayers().contains(player.getUniqueId())) {
                            p.sendMessage(quitMessageComponent);
                        }
                    });
                });

                plugin.getServer().getConsoleCommandSource().sendMessage(quitMessageComponent);
            }

            profile.setLastSeen(System.currentTimeMillis());

            playerManager.saveProfile(profile).join();
            plugin.getPlayerManager().handleProxyQuit(player.getUniqueId());

            plugin.getChatManager().clearPlayerReplies(player);
        });
    }

    private Component deserialize(String message, String playerName) {
        return MiniMessage.miniMessage().deserialize(message, Placeholder.unparsed("player", playerName));
    }

    private String getLastSeen(long time) {
        if (time == 0) return "jamais";
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

        if(message.contains("expulsé")) {
            player.disconnect(serverKickReason);
        }
    }
}