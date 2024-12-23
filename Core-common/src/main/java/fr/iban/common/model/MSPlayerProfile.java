package fr.iban.common.model;

import com.google.gson.Gson;
import fr.iban.common.enums.Option;

import java.util.*;

public class MSPlayerProfile extends MSPlayer {

    private JsonData jsonData = new JsonData();
    private String ip;

    public MSPlayerProfile(UUID uuid) {
        super(uuid);
    }

    private static class JsonData {
        private boolean vanished;
        private final Set<UUID> ignoredPlayers = new HashSet<>();
        private final Set<Integer> blackListedAnnounces = new HashSet<>();
        private final Map<Option, Boolean> options = new HashMap<>();
    }

    public boolean isVanished() {
        return jsonData.vanished;
    }

    public void setVanished(boolean vanished) {
        jsonData.vanished = vanished;
    }

    public Set<UUID> getIgnoredPlayers() {
        return jsonData.ignoredPlayers;
    }

    public Set<Integer> getBlackListedAnnounces() {
        return jsonData.blackListedAnnounces;
    }

    public Map<Option, Boolean> getOptions() {
        return jsonData.options;
    }

    public void setOption(Option option, boolean value) {
        jsonData.options.put(option, value);
    }

    public boolean getOption(Option option) {
        return jsonData.options.getOrDefault(option, false);
    }

    public void toggleOption(Option option) {
        jsonData.options.put(option, !getOption(option));
    }

    public String toJson() {
        return new Gson().toJson(jsonData);
    }

    public void fromJson(String json) {
        this.jsonData = new Gson().fromJson(json, JsonData.class);
        if (this.jsonData == null) {
            this.jsonData = new JsonData();
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}