package fr.iban.velocitycore.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private final CoreVelocityPlugin plugin;
    private final PlayerManager playerManager;
    private final ProxyServer server;
    private boolean isMuted = false;
    private final String pingPrefix;
    private final Map<Player, Player> replies = new ConcurrentHashMap<>();
    private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().extractUrls().build();
    private final Set<UUID> staffChatDisabledPlayers = new HashSet<>();

    public ChatManager(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.playerManager = plugin.getPlayerManager();
        this.pingPrefix = plugin.getConfig().getString("ping-prefix", "&e");
    }

    public void sendGlobalMessage(UUID senderUUID, String message) {
        Player sender = server.getPlayer(senderUUID).orElseThrow();

        if (message.startsWith("$") && sender.hasPermission("servercore.staffchat")) {
            sendStaffMessage(sender, message.substring(1));
            return;
        }

        if (isMuted && !sender.hasPermission("servercore.chatmanage")) {
            return;
        }

        if (sender.hasPermission("servercore.colors")) {
            message = componentToLegacy(parseMineDownInlineFormatting(message));
        }

        MSPlayerProfile senderProfile = playerManager.getProfile(senderUUID);

        if (!senderProfile.getOption(Option.CHAT)) {
            sender.sendMessage(MineDown.parse(Lang.get("chat.disabled")));
            logMessage(MineDown.parse("§8[§CDÉSACTIVÉ§8]§r " + message));
            return;
        }

        String finalMessage = message;
        replacePlaceHolders(plugin.getConfig().getString("chat-format").trim(), sender).thenAccept(chatFormat -> {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(chatFormat);

            for (Player receiverPlayer : server.getAllPlayers()) {
                String pmessage = finalMessage;
                UUID receiverUUID = receiverPlayer.getUniqueId();
                MSPlayerProfile receiverProfile = playerManager.getProfile(receiverUUID);
                String receiverUsername = receiverPlayer.getUsername();

                if (!receiverProfile.getOption(Option.CHAT) || receiverProfile.getIgnoredPlayers().contains(sender.getUniqueId())) {
                    continue;
                }

                if (pmessage.toLowerCase().contains(receiverUsername.toLowerCase()) && receiverProfile.getOption(Option.MENTION)) {
                    String ping = pingPrefix + receiverUsername;
                    String legacyFormattedPing = componentToLegacy(MineDown.parse(ping));
                    receiverPlayer.playSound(Sound.sound(Key.key("block.note_block.guitar"), Sound.Source.MASTER, 1f, 0.5f));
                    pmessage = pmessage.replace(receiverUsername, legacyFormattedPing + "§f");
                }

                Component messageComponent = componentFromLegacy(pmessage);
                Component finalMessageComponent = Component.empty().append(prefixComponent).append(messageComponent);

                receiverPlayer.sendMessage(finalMessageComponent);
            }

            logMessage(prefixComponent.append(componentFromLegacy(finalMessage)));
        }).exceptionally(e -> {
            plugin.getLogger().error("Error while sending global message", e);
            return null;
        });
    }

    private CompletableFuture<String> replacePlaceHolders(String message, Player sender) {
        final PlaceholderAPI api = PlaceholderAPI.createInstance();
        message = message.replace("%player%", sender.getUsername());
        message = message.replace("%premium%", getPremiumString(sender));

        return api.formatPlaceholders(message, sender.getUniqueId());
    }

    public void sendAnnonce(UUID uuid, String annonce) {
        Player player = server.getPlayer(uuid).orElse(null);

        if (player == null) {
            return;
        }

        if (isMuted && !player.hasPermission("servercore.chatmanage")) {
            return;
        }

        plugin.getServer().sendMessage(MineDown.parse(
                Lang.get("chat.annonce")
                        .replace("%player%", player.getUsername())
                        .replace("%message%", annonce)
        ));
    }

    private void sendStaffMessage(Player sender, String message) {
        String prefix = plugin.getConfig().getString("staff-chat-format");
        replacePlaceHolders(prefix, sender).thenAccept(chatFormat -> {
            String chatPrefix = componentToLegacy(MiniMessage.miniMessage().deserialize(chatFormat));
            String messageComponent = componentToLegacy(MineDown.parse(message));
            Component fullMessage = componentFromLegacy(chatPrefix + messageComponent);

            plugin.getServer().getAllPlayers().forEach(p -> {
                if (p.hasPermission("servercore.staffchat") && !staffChatDisabledPlayers.contains(p.getUniqueId())) {
                    p.sendMessage(fullMessage);
                }
            });

            logMessage(fullMessage);
        });
    }

    public void toggleChat(Player sender) {
        isMuted = !isMuted;
        if (isMuted) {
            plugin.getServer().sendMessage(MineDown.parse(Lang.get("chat.muted")));
        } else {
            plugin.getServer().sendMessage(MineDown.parse(Lang.get("chat.unmuted")));
        }
    }

    public void sendMessage(Player sender, Player target, String message) {
        UUID senderUUID = sender.getUniqueId();
        String senderName = sender.getUsername();
        String targetName = target.getUsername();
        MSPlayerProfile targetProfile = playerManager.getProfile(target.getUniqueId());

        if (!targetProfile.getOption(Option.MSG) && !sender.hasPermission("servercore.msgtogglebypass")) {
            sender.sendMessage(MineDown.parse(Lang.get("msg.disabled").replace("%player%", targetName)));
            return;
        }

        if (targetProfile.getIgnoredPlayers().contains(senderUUID)) {
            sender.sendMessage(MineDown.parse(Lang.get("msg.ignored")));
            return;
        }

        Component senderComponent;
        Component targetComponent;

        if (target.hasPermission("servercore.staff")) {
            senderComponent = MineDown.parse(Lang.get("msg.sender-staff")
                    .replace("%target%", targetName)
                    .replace("%message%", message));
        } else {
            senderComponent = MineDown.parse(Lang.get("msg.sender")
                    .replace("%target%", targetName)
                    .replace("%message%", message));
        }
        if (sender.hasPermission("servercore.staff")) {
            targetComponent = MineDown.parse(Lang.get("msg.target-staff")
                    .replace("%sender%", senderName)
                    .replace("%message%", message));
        } else {
            targetComponent = MineDown.parse(Lang.get("msg.target")
                    .replace("%sender%", senderName)
                    .replace("%message%", message));
        }

        targetComponent = targetComponent
                .hoverEvent(HoverEvent.showText(Component.text(Lang.get("msg.hover"))))
                .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "));

        sender.sendMessage(senderComponent);
        target.sendMessage(targetComponent);
        logMessage(MineDown.parse("&c" + senderName + " &7 ➔  " + "&8" + targetName + " &6➤ " + "&7 " + message));
        replies.put(sender, target);
        replies.put(target, sender);
    }

    private String getPremiumString(Player player) {
        if (player.hasPermission("premium")) {
            return "✮ ";
        } else {
            return "";
        }
    }

    private Component parseMineDownInlineFormatting(String message, String... replacements) {
        return new MineDown(message)
                .disable(MineDownParser.Option.ADVANCED_FORMATTING)
                .disable(MineDownParser.Option.SIMPLE_FORMATTING)
                .replace(replacements)
                .toComponent();
    }

    private String componentToLegacy(Component component) {
        return legacyComponentSerializer.serialize(component);
    }

    private Component componentFromLegacy(String legacy) {
        return legacyComponentSerializer.deserialize(legacy);
    }

    private void logMessage(Component message) {
        server.getConsoleCommandSource().sendMessage(message);
    }

    @Nullable
    public Player getPlayerToReply(Player player) {
        return replies.get(player);
    }

    public void clearPlayerReplies(Player player) {
        replies.remove(player);

        for (Map.Entry<Player, Player> entry : replies.entrySet()) {
            if (entry.getValue().equals(player)) {
                replies.remove(entry.getKey());
            }
        }
    }

    public void toggleStaffChat(Player player) {
        if (staffChatDisabledPlayers.contains(player.getUniqueId())) {
            staffChatDisabledPlayers.remove(player.getUniqueId());
            player.sendMessage(MineDown.parse(Lang.get("staffchat.enabled")));
        } else {
            staffChatDisabledPlayers.add(player.getUniqueId());
            player.sendMessage(MineDown.parse(Lang.get("staffchat.disabled")));
        }
    }
}
