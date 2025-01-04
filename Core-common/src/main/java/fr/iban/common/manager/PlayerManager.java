package fr.iban.common.manager;

import fr.iban.common.data.dao.MSPlayerDAO;
import fr.iban.common.messaging.AbstractMessagingManager;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.messaging.Message;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerManager {

    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
    protected final Map<UUID, MSPlayer> playersByUUID = new ConcurrentHashMap<>();
    protected final Map<String, MSPlayer> playersByName = new ConcurrentHashMap<>();
    protected final Map<UUID, MSPlayerProfile> profiles = new ConcurrentHashMap<>();
    protected Set<UUID> onlinePlayers = ConcurrentHashMap.newKeySet();

    protected final MSPlayerDAO dao;

    protected AbstractMessagingManager messagingManager;

    public PlayerManager(AbstractMessagingManager messagingManager) {
        this.dao = new MSPlayerDAO();
        this.messagingManager = messagingManager;
        load();
    }

    public void load() {
        CompletableFuture.runAsync(() -> {
                    dao.getOfflinePlayers().forEach(msPlayer -> {
                        playersByUUID.put(msPlayer.getUniqueId(), msPlayer);
                        playersByName.put(msPlayer.getName(), msPlayer);
                    });

                    log.info("Loaded {} players from the database.", playersByUUID.size());

                    dao.getOnlinePlayerIds().forEach(uuid -> {
                        profiles.put(uuid, dao.getPlayerProfile(uuid));
                        onlinePlayers.add(uuid);
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public MSPlayerProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    public MSPlayerProfile getProfile(String name) {
        return profiles.values().stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public MSPlayer getOfflinePlayer(UUID uuid) {
        return playersByUUID.get(uuid);
    }

    public MSPlayer getOfflinePlayer(String name) {
        return playersByName.get(name);
    }

    public Set<String> getOnlinePlayerNames() {
        return onlinePlayers.stream()
                .map(uuid -> playersByUUID.get(uuid).getName())
                .collect(Collectors.toSet());
    }

    public CompletableFuture<Void> saveProfile(MSPlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            dao.savePlayerProfile(profile);
            messagingManager.sendMessage(CoreChannel.SYNC_PLAYER_CHANNEL, profile);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public MSPlayerProfile loadProfile(UUID uuid, String name) {
        MSPlayerProfile profile = dao.getPlayerProfile(uuid, name);

        playersByUUID.put(uuid, profile);
        playersByName.put(profile.getName(), profile);
        profiles.put(uuid, profile);

        return profile;
    }

    public void handleSync(Message message) {
        MSPlayerProfile profile = message.getMessage(MSPlayerProfile.class);
        profiles.put(profile.getUniqueId(), profile);
        playersByUUID.put(profile.getUniqueId(), profile);
        playersByName.put(profile.getName(), profile);
    }

    public void clearOnlinePlayers() {
        profiles.clear();
        dao.clearOnlinePlayers();
    }

    public void handleProxyJoin(MSPlayerProfile profile) {
        UUID uuid = profile.getUniqueId();

        dao.addOnlinePlayer(uuid);
        dao.saveLoginToDb(uuid, System.currentTimeMillis(), profile.getIp());
        messagingManager.sendMessage(CoreChannel.PLAYER_JOIN_CHANNEL, uuid.toString());
    }

    public void handleProxyQuit(UUID uniqueId) {
        dao.removeOnlinePlayer(uniqueId);
        onlinePlayers.remove(uniqueId);
        messagingManager.sendMessage(CoreChannel.PLAYER_QUIT_CHANNEL, uniqueId.toString());
    }

    public void handlePlayerJoin(UUID uuid) {
        onlinePlayers.add(uuid);
    }

    public void handlePlayerQuit(UUID uuid) {
        onlinePlayers.remove(uuid);
    }

    public Set<MSPlayerProfile> getProfiles() {
        return new HashSet<>(profiles.values());
    }

    public boolean isOnline(UUID uuid) {
        return onlinePlayers.contains(uuid);
    }
}
