package fr.iban.common.manager;

import fr.iban.common.data.dao.MSPlayerDAO;
import fr.iban.common.messaging.AbstractMessagingManager;
import fr.iban.common.messaging.CoreChannel;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerManager {

    protected final Map<UUID, MSPlayer> playersByUUID = new ConcurrentHashMap<>();
    protected final Map<String, MSPlayer> playersByName = new ConcurrentHashMap<>();
    protected final Map<UUID, MSPlayerProfile> profiles = new ConcurrentHashMap<>();

    protected final MSPlayerDAO dao;

    protected AbstractMessagingManager messagingManager;

    public PlayerManager(AbstractMessagingManager messagingManager) {
        this.dao = new MSPlayerDAO();
        this.messagingManager = messagingManager;
        load();
    }

    public void load() {
        CompletableFuture.runAsync(() -> {
            dao.getOfflinePlayers().forEach(player -> {
                playersByUUID.put(player.getUniqueId(), player);
                playersByName.put(player.getName(), player);
            });

            dao.getOnlinePlayerIds().forEach(uuid -> {
                profiles.put(uuid, dao.getPlayerProfile(uuid));
            });
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
        return profiles.values().stream()
                .filter(MSPlayerProfile::isOnline)
                .map(MSPlayer::getName)
                .collect(Collectors.toSet());
    }

    public CompletableFuture<Void> saveProfile(MSPlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            dao.saveMSPlayer(profile);

            playersByUUID.put(profile.getUniqueId(), profile);
            playersByName.put(profile.getName(), profile);
            profiles.put(profile.getUniqueId(), profile);

            messagingManager.sendMessage(CoreChannel.SYNC_PLAYER_CHANNEL, profile.getUniqueId());
        });
    }

    public MSPlayerProfile loadProfile(UUID uuid) {
        MSPlayerProfile profile = dao.getPlayerProfile(uuid);

        playersByUUID.put(uuid, profile);
        playersByName.put(profile.getName(), profile);
        profiles.put(uuid, new MSPlayerProfile(profile));

        return profile;
    }

    public void clearOnlinePlayers() {
        profiles.clear();
        dao.clearOnlinePlayers();
    }

    public void handleProxyJoin(UUID uuid) {
        dao.addOnlinePlayer(uuid);
        messagingManager.sendMessage(CoreChannel.PLAYER_JOIN_CHANNEL, uuid.toString());
    }

    public void handleProxyQuit(UUID uniqueId) {
        dao.removeOnlinePlayer(uniqueId);
        messagingManager.sendMessage(CoreChannel.PLAYER_QUIT_CHANNEL, uniqueId.toString());
    }

    public void handlePlayerJoin(UUID uuid) {
        MSPlayerProfile profile = profiles.get(uuid);

        if(profile == null) {
            profile = dao.getPlayerProfile(uuid);
        }

        profile.setOnline(true);
    }

    public void handlePlayerQuit(UUID uuid) {
        profiles.get(uuid).setOnline(false);
    }

    public Set<MSPlayerProfile> getProfiles() {
        return new HashSet<>(profiles.values());
    }

    public boolean isOnline(UUID uuid) {
        return profiles.containsKey(uuid) && profiles.get(uuid).isOnline();
    }
}
