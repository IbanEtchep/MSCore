package fr.iban.common.model;

import com.google.gson.Gson;
import fr.iban.common.enums.Option;
import fr.iban.common.teleport.SLocation;

import java.util.*;

public class MSPlayerProfile extends MSPlayer {

    private JsonData data = new JsonData();

    public MSPlayerProfile(UUID uuid) {
        super(uuid);
    }

    private static class JsonData {
        private String ip;
        private boolean vanished;
        private SLocation deathLocation;
        private SLocation lastRTPLocation;
        private SLocation lastSurvivalLocation;
        private final Set<UUID> ignoredPlayers = new HashSet<>();
        private final Set<Integer> blackListedAnnounces = new HashSet<>();
        private final Map<Option, Boolean> options = new HashMap<>();
    }

    public boolean isVanished() {
        return data.vanished;
    }

    public void setVanished(boolean vanished) {
        data.vanished = vanished;
    }

    public SLocation getDeathLocation() {
        return data.deathLocation;
    }

    public void setDeathLocation(SLocation deathLocation) {
        data.deathLocation = deathLocation;
    }

    public SLocation getLastRTPLocation() {
        return data.lastRTPLocation;
    }

    public void setLastRTPLocation(SLocation lastRTPLocation) {
        data.lastRTPLocation = lastRTPLocation;
    }

    public SLocation getLastSurvivalLocation() {
        return data.lastSurvivalLocation;
    }

    public void setLastSurvivalLocation(SLocation lastSurvivalLocation) {
        data.lastSurvivalLocation = lastSurvivalLocation;
    }

    public Set<UUID> getIgnoredPlayers() {
        return data.ignoredPlayers;
    }

    public Set<Integer> getBlackListedAnnounces() {
        return data.blackListedAnnounces;
    }

    public boolean getOption(Option option) {
        return data.options.getOrDefault(option, option.getDefaultValue());
    }

    public void setOption(Option option, boolean value) {
        // Only save the option if it's different from the default value
        if (value != option.getDefaultValue()) {
            data.options.put(option, value);
        } else {
            data.options.remove(option);
        }
    }

    public void toggleOption(Option option) {
        setOption(option, !getOption(option));
    }

    public String getIp() {
        return data.ip;
    }

    public void setIp(String ip) {
        data.ip = ip;
    }

    public static void validateJsonData(String json) {
        JsonData testData = new Gson().fromJson(json, JsonData.class);
        if (testData == null) {
            throw new IllegalStateException("Deserialized data is null");
        }
    }

    public void setDataFromJson(String json) {
        if (json != null) {
            this.data = new Gson().fromJson(json, JsonData.class);
            if (this.data == null) {
                this.data = new JsonData();
            }
        }
    }

    public String dataToJson() {
        return new Gson().toJson(data);
    }

}