package fr.iban.common.model;

import fr.iban.common.enums.Option;

import java.util.*;

public class MSPlayerProfile extends  MSPlayer {

    private Set<Integer> blackListedAnnounces = new HashSet<>();
    private Set<UUID> ignoredPlayers = new HashSet<>();
    private Map<Option, Boolean> options = new HashMap<>();
    private String ip;

    private boolean vanished;
    private boolean online;

    public MSPlayerProfile(UUID uuid) {
        super(uuid);
    }

    public MSPlayerProfile(MSPlayer msPlayer) {
        super(msPlayer.getUniqueId(), msPlayer.getName());
    }

    public void setBlackListedAnnounces(Set<Integer> blackListedAnnounces) {
        this.blackListedAnnounces = blackListedAnnounces;
    }

    public void setIgnoredPlayers(Set<UUID> ignoredPlayers) {
        this.ignoredPlayers = ignoredPlayers;
    }

    public void setOptions(Map<Option, Boolean> options) {
        this.options = options;
    }

    public void setOption(Option option, boolean value) {
        options.put(option, value);
    }

    public void toggleOption(Option option) {
        options.put(option, !getOption(option));
    }

    public boolean getOption(Option option) {
        return options.getOrDefault(option, option.getDefaultValue());
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Set<Integer> getBlackListedAnnounces() {
        if (blackListedAnnounces == null) {
            blackListedAnnounces = new HashSet<>();
        }
        return blackListedAnnounces;
    }

    public Set<UUID> getIgnoredPlayers() {
        if (ignoredPlayers == null) {
            ignoredPlayers = new HashSet<>();
        }

        return ignoredPlayers;
    }

    public Map<Option, Boolean> getOptions() {
        if (options == null) {
            options = new HashMap<>();
        }
        return options;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }
}
