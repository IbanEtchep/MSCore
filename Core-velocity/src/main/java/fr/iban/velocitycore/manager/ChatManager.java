package fr.iban.velocitycore.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import fr.iban.common.chat.ChatItemRenderer;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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
        sendGlobalMessage(senderUUID, message, null);
    }

    public void sendGlobalMessage(UUID senderUUID, String message, @Nullable String itemJson) {
        Player sender = server.getPlayer(senderUUID).orElseThrow();

        Component itemComponent = itemJson == null ? null : buildItemComponent(itemJson);

        if (message.startsWith("$") && sender.hasPermission("servercore.staffchat")) {
            sendStaffMessage(sender, message.substring(1), itemComponent);
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
            sender.sendMessage(MineDown.parse("&cVous ne pouvez pas envoyer ce message car votre tchat est désactivé"));
            logMessage(MineDown.parse("§8[§CDÉSACTIVÉ§8]§r " + message));
            return;
        }

        String finalMessage = message;
        Component finalItemComponent = itemComponent;
        replacePlaceHolders(plugin.getConfig().getString("chat-format").trim(), sender).thenAccept(chatFormat -> {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(chatFormat);

            for (MSPlayerProfile receiverProfile : playerManager.getProfiles()) {
                Player receiverPlayer = server.getPlayer(receiverProfile.getUniqueId()).orElse(null);
                if (receiverPlayer == null) continue;

                String pmessage = finalMessage;
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

                Component messageComponent = ChatItemRenderer.inject(componentFromLegacy(pmessage), finalItemComponent);
                Component finalMessageComponent = Component.empty().append(prefixComponent).append(messageComponent);

                receiverPlayer.sendMessage(finalMessageComponent);
            }

            Component logItemComponent = finalItemComponent == null ? null : Component.text("[item]");
            Component loggedMessage = ChatItemRenderer.inject(componentFromLegacy(finalMessage), logItemComponent);
            logMessage(prefixComponent.append(loggedMessage));
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
        sendAnnonce(uuid, annonce, null);
    }

    public void sendAnnonce(UUID uuid, String annonce, @Nullable String itemJson) {
        Player player = server.getPlayer(uuid).orElse(null);

        if (player == null) {
            return;
        }

        if (isMuted && !player.hasPermission("servercore.chatmanage")) {
            return;
        }

        Component itemComponent = itemJson == null ? null : buildItemComponent(itemJson);

        String defaultFormat = plugin.getConfig().getString("announce-format",
                "<color:#f07e71><bold>Annonce de <color:#fbb29e><bold><player></bold></color> <color:#f07e71>➤ <color:#7bc8fe><bold><message>");
        String premiumFormat = plugin.getConfig().getString("announce-premium-format", defaultFormat);

        MiniMessage mini = MiniMessage.miniMessage();

        for (Player target : plugin.getServer().getAllPlayers()) {
            String format = target.hasPermission("premium") ? premiumFormat : defaultFormat;
            Component component = mini.deserialize(format,
                    Placeholder.unparsed("player", player.getUsername()),
                    Placeholder.parsed("message", annonce)
            );
            component = ChatItemRenderer.inject(component, itemComponent);
            target.sendMessage(component);
        }
    }

    private void sendStaffMessage(Player sender, String message, @Nullable Component itemComponent) {
        String prefix = plugin.getConfig().getString("staff-chat-format");
        replacePlaceHolders(prefix, sender).thenAccept(chatFormat -> {
            String chatPrefix = componentToLegacy(MiniMessage.miniMessage().deserialize(chatFormat));
            String messageComponent = componentToLegacy(MineDown.parse(message));
            Component base = componentFromLegacy(chatPrefix + messageComponent);
            Component fullMessage = ChatItemRenderer.inject(base, itemComponent);

            plugin.getServer().getAllPlayers().forEach(p -> {
                if (p.hasPermission("servercore.staffchat") && !staffChatDisabledPlayers.contains(p.getUniqueId())) {
                    p.sendMessage(fullMessage);
                }
            });

            Component loggedMessage = ChatItemRenderer.inject(base, itemComponent == null ? null : Component.text("[item]"));
            logMessage(loggedMessage);
        });
    }

    public void toggleChat(Player sender) {
        isMuted = !isMuted;
        if (isMuted) {
            plugin.getServer().sendMessage(MineDown.parse("&cLe chat est désormais muet."));
        } else {
            plugin.getServer().sendMessage(MineDown.parse("&aLe chat n'est plus muet."));
        }
    }

    public void sendMessage(Player sender, Player target, String message) {
        UUID senderUUID = sender.getUniqueId();
        String senderName = sender.getUsername();
        String targetName = target.getUsername();
        MSPlayerProfile targetProfile = playerManager.getProfile(target.getUniqueId());

        if (targetProfile == null || (!targetProfile.getOption(Option.MSG) && !sender.hasPermission("servercore.msgtogglebypass"))) {
            sender.sendMessage(MineDown.parse("&c" + target.getUsername() + " a désactivé ses messages"));
            return;
        }

        if (targetProfile.getIgnoredPlayers().contains(senderUUID)) {
            sender.sendMessage(MineDown.parse("&cVous ne pouvez pas envoyer de message à ce joueur"));
            return;
        }

        Component senderComponent;
        Component targetComponent;

        if (target.hasPermission("servercore.staff")) {
            senderComponent = MineDown.parse("&8Moi &7➔ &8[&6Staff&8] &c" + targetName + " &6➤&7 " + message);
        } else {
            senderComponent = MineDown.parse("&8Moi &7➔ &c" + targetName + " &6➤&7 " + message);
        }
        if (sender.hasPermission("servercore.staff")) {
            targetComponent = MineDown.parse("&8[&6Staff&8] &c" + senderName + " &7➔ &8Moi &6➤&7 " + message);
        } else {
            targetComponent = MineDown.parse("&c" + senderName + " &7➔ &8Moi &6➤&7 " + message);
        }

        targetComponent = targetComponent
                .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour répondre")))
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

    /**
     * Construit le composant visuel de l'item à partir du JSON reçu du backend : extrait le nom
     * (sans les crochets natifs), l'insère dans le format configurable "item-format" (MiniMessage,
     * placeholder %item%), et réapplique le survol natif (showItem) sur l'ensemble.
     */
    private Component buildItemComponent(String itemJson) {
        Component display = GsonComponentSerializer.gson().deserialize(itemJson);
        HoverEvent<?> hover = display.hoverEvent();
        Component name = extractItemName(display);
        String format = plugin.getConfig().getString("item-format", "[%item%]");
        Component formatted = MiniMessage.miniMessage().deserialize(
                format.replace("%item%", "<item>"),
                Placeholder.component("item", name)
        );
        if (hover != null) {
            formatted = formatted.hoverEvent(hover);
        }
        return formatted;
    }

    /**
     * Le displayName() vanilla d'un item est un composant traduisible "chat.square_brackets" dont
     * le seul argument est le nom de l'item. On en extrait ce nom pour pouvoir contrôler les
     * crochets via la config. En cas de structure inattendue, on retombe sur le composant complet.
     */
    private Component extractItemName(Component display) {
        if (display instanceof TranslatableComponent translatable && !translatable.arguments().isEmpty()) {
            return translatable.arguments().get(0).asComponent();
        }
        return display;
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
            player.sendMessage(MineDown.parse("&aVous pouvez à nouveau recevoir des messages du staff"));
        } else {
            staffChatDisabledPlayers.add(player.getUniqueId());
            player.sendMessage(MineDown.parse("&cVous ne pouvez plus recevoir les messages du staff"));
        }
    }
}