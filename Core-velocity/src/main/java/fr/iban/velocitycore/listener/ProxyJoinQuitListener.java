package fr.iban.velocitycore.listener;

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
                    "%s s'est connecté !",
                    "%s est dans la place !",
                    "%s a rejoint le serveur !",
                    "Un %s sauvage apparaît ! ",
                    "Tout le monde, dites bonjour à %s !",
                    "%s vient d'arriver, faites place !"
            };

    private final String[] quitMessages =
            {
                    "%s nous a quitté :(",
                    "%s s'est déconnecté.",
                    "%s a disparu dans l'ombre.",
                    "Et voilà, %s est parti."
            };

    private final String[] longAbsenceMessages = {
            "%s est enfin de retour !",
            "Oh mon dieu, %s est revenu !",
            "Le prodige %s est de retour !",
            "%s, notre légende perdue est enfin de retour !",
            "Vous vous rappelez de %s ? Eh bien, devinez qui vient de revenir !"
    };

    private final String[] firstJoinMessages = {
            "Bienvenue %s, content de te voir pour la première fois !",
            "%s vient de franchir les portes du serveur pour la première fois !",
            "C'est la grande première de %s sur le serveur !",
            "Hey tout le monde, accueillons notre nouveau membre, %s !",
            "Un nouveau visage ! Salut %s, bienvenue sur le serveur !"
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
                String joinMessage = "&8[&a+&8] &8";
                if ((System.currentTimeMillis() - lastSeen) > 2592000000L) {
                    joinMessage += String.format(ArrayUtils.getRandomFromArray(longAbsenceMessages), playerName);
                } else {
                    joinMessage += String.format(ArrayUtils.getRandomFromArray(joinMessages), playerName);
                }

                Component message = MineDown.parse(joinMessage).hoverEvent(HoverEvent.showText(
                        Component.text("Vu pour la dernière fois " + getLastSeen(lastSeen), NamedTextColor.GRAY)
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
            String firstJoinMessage = "&8≫ &7" + String.format(ArrayUtils.getRandomFromArray(firstJoinMessages), playerName);
            Component welcomeComponent = MineDown.parse(firstJoinMessage)
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
    public void onDisconnect(DisconnectEvent e) {
        Player player = e.getPlayer();
        ProxyServer proxy = plugin.getServer();

        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());
        String quitMessage = "&8[&c-&8] &8" + String.format(ArrayUtils.getRandomFromArray(quitMessages), player.getUsername());
        Component quitMessageComponent = MineDown.parse(quitMessage);

        if ((System.currentTimeMillis() - account.getLastSeen()) > 60000) {
            proxy.getAllPlayers().forEach(p -> {
                MSPlayerProfile account2 = playerManager.getProfile(p.getUniqueId());
                if (account2.getOption(Option.LEAVE_MESSAGE) && !account2.getIgnoredPlayers().contains(player.getUniqueId())) {
                    p.sendMessage(quitMessageComponent);
                }
            });

            plugin.getServer().getConsoleCommandSource().sendMessage(quitMessageComponent);
        }

        account.setLastSeen(System.currentTimeMillis());
        playerManager.saveProfile(account)
                .thenRun(() -> plugin.getPlayerManager().handleProxyQuit(player.getUniqueId()))
                .exceptionally(e1 -> {
                    e1.printStackTrace();
                    return null;
                });

        plugin.getChatManager().clearPlayerReplies(player);
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