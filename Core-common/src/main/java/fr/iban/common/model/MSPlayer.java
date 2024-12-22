package fr.iban.common.model;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class MSPlayer {

    private final UUID uuid;
    private String name;
    private long lastSeen;

    public MSPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public MSPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastSeen(Date date) {
        this.lastSeen = date.getTime();
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Date getLastSeen() {
        return new Date(lastSeen);
    }

    public long getLastSeenTimestamp() {
        return lastSeen;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MSPlayer msPlayer = (MSPlayer) o;
        return Objects.equals(uuid, msPlayer.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
