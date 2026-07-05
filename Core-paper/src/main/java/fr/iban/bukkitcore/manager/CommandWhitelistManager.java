package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.TrustedCommand;
import fr.iban.common.messaging.Message;
import fr.iban.common.messaging.message.WhitelistReply;
import fr.iban.common.messaging.message.WhitelistRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandWhitelistManager implements Listener {

    private static final String REQUEST_CHANNEL = "CommandWhitelistRequestChannel";
    private static final String REPLY_CHANNEL = "CommandWhitelistReplyChannel";

    private final CoreBukkitPlugin plugin;
    private final MessagingManager messagingManager;
    private final Map<UUID, WhitelistRequest> requests = new ConcurrentHashMap<>();
    private final Set<String> alreadyRequested = ConcurrentHashMap.newKeySet();

    public CommandWhitelistManager(CoreBukkitPlugin plugin, MessagingManager messagingManager) {
        this.plugin = plugin;
        this.messagingManager = messagingManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startCleaningTask();
    }

    public void request(String command, String context, String playerName) {
        String key = context + ":" + command.toLowerCase();
        if (!alreadyRequested.add(key)) {
            return;
        }
        WhitelistRequest request = new WhitelistRequest(UUID.randomUUID(), command, context, playerName);
        requests.put(request.getRequestId(), request);
        messagingManager.sendMessage(REQUEST_CHANNEL, request);
    }

    private void handleReply(WhitelistReply reply) {
        WhitelistRequest request = requests.remove(reply.getRequestId());
        if (request == null) {
            return;
        }
        String category = reply.getCategory();
        if (category.equals("staff") || category.equals("player")) {
            TrustedCommand trustedCommand = new TrustedCommand(request.getCommand(), category, request.getContext());
            if (!plugin.getTrustedCommandManager().getTrustedCommands().contains(trustedCommand)) {
                plugin.getTrustedCommandManager().addTrustedCommand(trustedCommand);
            }
        }
    }

    private void startCleaningTask() {
        plugin.getScheduler().runTimerAsync(task ->
                requests.values().removeIf(r -> System.currentTimeMillis() - r.getCreatedAt() > 600000L),
                1200L, 1200L);
    }

    @EventHandler
    public void onCoreMessage(CoreMessageEvent e) {
        Message message = e.getMessage();
        if (!message.getChannel().equals(REPLY_CHANNEL)) {
            return;
        }
        handleReply(message.getMessage(WhitelistReply.class));
    }
}
