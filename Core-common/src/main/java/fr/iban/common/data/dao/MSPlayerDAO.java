package fr.iban.common.data.dao;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MSPlayerDAO {

    private static final Logger logger = LoggerFactory.getLogger(MSPlayerDAO.class);
    private final Jdbi jdbi;

    public MSPlayerDAO() {
        this.jdbi = DbAccess.getJdbi();
    }

    public MSPlayerProfile getPlayerProfile(UUID uuid) {
        return jdbi.inTransaction(handle -> {
            try {
                return handle.createQuery("""
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
                                try {
                                    MSPlayerProfile.validateJsonData(jsonData);
                                    player.setDataFromJson(jsonData);
                                    saveBackup(handle, uuid, jsonData, true, null);
                                } catch (Exception e) {
                                    logger.error("Failed to parse data for " + uuid, e);
                                    saveBackup(handle, uuid, jsonData, false, e.getMessage());
                                    loadLastValidBackup(handle, uuid).ifPresent(player::setDataFromJson);
                                }
                            }
                            return player;
                        })
                        .findFirst()
                        .orElseGet(() -> new MSPlayerProfile(uuid));
            } catch (Exception e) {
                logger.error("Failed to load player " + uuid, e);
                return new MSPlayerProfile(uuid);
            }
        });
    }

    public void savePlayerProfile(MSPlayerProfile profile) {
        jdbi.useTransaction(handle -> {
            String json = profile.dataToJson();
            try {
                // Valide le JSON avant de sauvegarder
                MSPlayerProfile.validateJsonData(json);

                handle.createUpdate("""
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
                        .bind("data", json)
                        .execute();

                saveBackup(handle, profile.getUniqueId(), json, true, null);
            } catch (Exception e) {
                logger.error("Failed to save player " + profile.getUniqueId(), e);
                throw e;
            }
        });
    }

    private void saveBackup(Handle handle, UUID uuid, String json, boolean valid, String error) {
        handle.createUpdate("""
                INSERT INTO sc_players_backup (player_uuid, data, valid, error)
                VALUES (:uuid, :data, :valid, :error)
                """)
                .bind("uuid", uuid.toString())
                .bind("data", json)
                .bind("valid", valid)
                .bind("error", error)
                .execute();

        handle.createUpdate("""
                DELETE FROM sc_players_backup 
                WHERE player_uuid = :uuid 
                AND id NOT IN (
                    SELECT id FROM (
                        SELECT id 
                        FROM sc_players_backup 
                        WHERE player_uuid = :uuid 
                        ORDER BY created_at DESC 
                        LIMIT 5
                    ) tmp
                )
                """)
                .bind("uuid", uuid.toString())
                .execute();
    }

    private Optional<String> loadLastValidBackup(Handle handle, UUID uuid) {
        return handle.createQuery("""
        SELECT data 
        FROM sc_players_backup 
        WHERE player_uuid = :uuid 
        AND valid = true 
        ORDER BY created_at DESC 
        LIMIT 1
        """)
                .bind("uuid", uuid.toString())
                .mapTo(String.class)
                .findFirst();
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