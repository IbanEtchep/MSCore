package fr.iban.common.data.dao;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;
import org.jdbi.v3.core.Jdbi;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MSPlayerDAO {

    private final Jdbi jdbi;

    public MSPlayerDAO() {
        this.jdbi = DbAccess.getJdbi();
    }

    public MSPlayerProfile getPlayerProfile(UUID uuid) {
        return jdbi.inTransaction(handle -> handle.createQuery("""
            SELECT * FROM sc_players 
            WHERE uuid = :uuid
            """)
                .bind("uuid", uuid.toString())
                .map((rs, ctx) -> {
                    MSPlayerProfile player = new MSPlayerProfile(uuid);
                    player.setName(rs.getString("name"));
                    player.setLastSeen(rs.getLong("lastseen"));

                    String jsonData = rs.getString("data");
                    if (jsonData != null) {
                        player.fromJson(jsonData);
                    }

                    return player;
                })
                .findFirst()
                .orElseGet(() -> new MSPlayerProfile(uuid)));
    }

    public void savePlayerProfile(MSPlayerProfile profile) {
        jdbi.useTransaction(handle -> handle.createUpdate("""
                    INSERT INTO sc_players (uuid, name, lastseen, data) 
                    VALUES (:uuid, :name, :lastSeen, :data) 
                    ON DUPLICATE KEY UPDATE 
                    name = COALESCE(:name, 'NonDefini'),
                    lastseen = :lastSeen,
                    data = :data
                    """)
                    .bind("uuid", profile.getUniqueId().toString())
                    .bind("name", profile.getName())
                    .bind("lastSeen", profile.getLastSeen())
                    .bind("data", profile.toJson())
                    .execute());
    }
    public Set<UUID> getOnlinePlayerIds() {
        return jdbi.withHandle(handle -> handle.createQuery("""
            SELECT uuid 
            FROM sc_players P 
            JOIN sc_online_players OP ON P.id = OP.player_id
            """)
                .map((rs, ctx) -> UUID.fromString(rs.getString("uuid")))
                .collect(Collectors.toSet())
        );
    }

    public Set<MSPlayer> getOfflinePlayers() {
        return jdbi.withHandle(handle -> handle.createQuery("""
            SELECT * 
            FROM sc_players P 
            LEFT JOIN sc_online_players OP ON P.id = OP.player_id
            WHERE OP.player_id IS NULL
            """).map((rs, ctx) -> {
                    MSPlayer player = new MSPlayer(UUID.fromString(rs.getString("uuid")));
                    player.setName(rs.getString("name"));
                    player.setLastSeen(rs.getLong("lastseen"));
                    return player;
                }).collect(Collectors.toSet())
        );
    }

    public void saveLoginToDb(UUID uuid, long lastSeen, String ip) {
        jdbi.useHandle(handle -> handle.createUpdate("""
            INSERT INTO sc_logins (id, date_time, ip) 
            VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid), :lastSeen, :ip) 
            ON DUPLICATE KEY UPDATE id = id
            """)
                .bind("uuid", uuid.toString())
                .bind("lastSeen", lastSeen)
                .bind("ip", ip)
                .execute()
        );
    }

    public void addOnlinePlayer(UUID uuid) {
        jdbi.useHandle(handle -> handle.createUpdate("""
            INSERT INTO sc_online_players (player_id) 
            VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid)) 
            ON DUPLICATE KEY UPDATE player_id = player_id
            """)
                .bind("uuid", uuid.toString()).execute()
        );
    }

    public void removeOnlinePlayer(UUID uuid) {
        jdbi.useHandle(handle -> handle.createUpdate("""
            DELETE FROM sc_online_players 
            WHERE player_id = (SELECT id FROM sc_players WHERE uuid = :uuid)
            """).bind("uuid", uuid.toString())
                .execute()
        );
    }

    public void clearOnlinePlayers() {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM sc_online_players").execute()
        );
    }
}