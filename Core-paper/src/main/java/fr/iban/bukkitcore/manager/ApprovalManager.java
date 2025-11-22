package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.TrustedUser;
import fr.iban.common.messaging.Message;
import fr.iban.common.messaging.message.ApprovalReply;
import fr.iban.common.messaging.message.ApprovalRequest;
import fr.iban.common.messaging.message.PlayerInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ApprovalManager implements Listener {

    private final CoreBukkitPlugin plugin;
    private final MessagingManager messagingManager;
    private final BukkitTrustedUserManager trustedUserManager;
    private final List<ApprovalRequest> requests = new ArrayList<>();

    public ApprovalManager(CoreBukkitPlugin plugin, MessagingManager messagingManager, BukkitTrustedUserManager trustedUserManager) {
        this.plugin = plugin;
        this.messagingManager = messagingManager;
        this.trustedUserManager = trustedUserManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startCleaningTask();
    }

    public List<ApprovalRequest> getRequests() {
        return requests;
    }

    private ApprovalRequest getRequest(UUID uuid) {
        return requests.stream().filter(requests -> requests.getRequestID().equals(uuid)).findFirst().orElse(null);
    }

    public void sendRequest(Player player, String requestMessage, Consumer<Boolean> result) {
        PlayerInfo playerInfo = new PlayerInfo(player.getUniqueId(), player.getName(), Objects.requireNonNull(player.getAddress()).getHostString());
        ApprovalRequest request = new ApprovalRequest(UUID.randomUUID(), playerInfo, requestMessage, result);
        String REQUEST_CHANNEL = "ApprovalRequestChannel";
        messagingManager.sendMessage(REQUEST_CHANNEL, request);
        requests.add(request);
    }

    private void handleReply(ApprovalReply reply) {
        ApprovalRequest request = getRequest(reply.getRequestID());
        if(request == null) return;

        request.consumeResult(reply.isResult());
        if(reply.isTrustUser()) {
            PlayerInfo playerInfo = request.getPlayerInfo();
            trustedUserManager.trustUser(new TrustedUser(playerInfo.getUuid(), playerInfo.getIp()));
        }
        requests.remove(request);
    }

    private void startCleaningTask() {
        plugin.getScheduler().runTimerAsync(task -> {
            requests.removeIf(request -> System.currentTimeMillis() - request.getCreatedAt() > 600000L);
        }, 1200L, 1200L);
    }

    @EventHandler
    public void onCoreMessage(CoreMessageEvent e) {
        Message message = e.getMessage();
        String REPLY_CHANNEL = "ApprovalReplyChannel";
        if(!message.getChannel().equals(REPLY_CHANNEL)) {
            return;
        }

        ApprovalReply reply = message.getMessage(ApprovalReply.class);
        handleReply(reply);
    }
}
