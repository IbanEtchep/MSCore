package fr.iban.common.data.dao;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.enums.Option;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MSPlayerDAO {
    private final Jdbi jdbi;

    public MSPlayerDAO() {
        this.jdbi = DbAccess.getJdbi();
    }

    public MSPlayerProfile getPlayerProfile(UUID uuid) {
        return jdbi.inTransaction(handle -> {
            MSPlayerProfile player = new MSPlayerProfile(uuid);

            // Données de base
            handle.createQuery("SELECT * FROM sc_players WHERE uuid = :uuid")
                    .bind("uuid", uuid.toString())
                    .map((rs, ctx) -> {
                        player.setName(rs.getString("name"));
                        player.setLastSeen(rs.getLong("lastseen"));
                        return player;
                    })
                    .findFirst();

            // Options
            Map<Option, Boolean> options = handle.createQuery("""
                SELECT idOption 
                FROM sc_players, sc_options 
                WHERE uuid = :uuid 
                AND sc_players.id = sc_options.id
                """)
                    .bind("uuid", uuid.toString())
                    .map((rs, ctx) -> Option.valueOf(rs.getString("idOption")))
                    .list()
                    .stream()
                    .collect(Collectors.toMap(
                            option -> option,
                            option -> !option.getDefaultValue()
                    ));
            player.setOptions(options);

            // Ignored players
            Set<UUID> ignored = new HashSet<>(handle.createQuery("""
                SELECT uuidPlayer 
                FROM sc_players, sc_ignored_players 
                WHERE uuid = :uuid 
                AND sc_players.id = sc_ignored_players.id
                """)
                    .bind("uuid", uuid.toString())
                    .map((rs, ctx) -> UUID.fromString(rs.getString("uuidPlayer")))
                    .list());
            player.setIgnoredPlayers(ignored);

            // Blacklisted announces
            Set<Integer> blacklist = new HashSet<>(handle.createQuery("""
                SELECT idAnnonce 
                FROM sc_players, sc_annonces_blacklist 
                WHERE uuid = :uuid 
                AND sc_players.id = sc_annonces_blacklist.id
                """)
                    .bind("uuid", uuid.toString())
                    .mapTo(Integer.class)
                    .list());
            player.setBlackListedAnnounces(blacklist);

            // Last IP
            String ip = handle.createQuery("""
                SELECT ip 
                FROM sc_logins 
                WHERE id = (SELECT id FROM sc_players WHERE uuid = :uuid) 
                ORDER BY date_time DESC LIMIT 1
                """)
                    .bind("uuid", uuid.toString())
                    .mapTo(String.class)
                    .findFirst()
                    .orElse(null);
            player.setIp(ip);

            return player;
        });
    }

    public void saveMSPlayer(MSPlayer player) {
        jdbi.useTransaction(handle -> {
            // Save basic player
            handle.createUpdate("""
                INSERT INTO sc_players (uuid, name, lastseen) 
                VALUES (:uuid, :name, :lastSeen) 
                ON DUPLICATE KEY UPDATE 
                name = COALESCE(:name, 'NonDefini'), 
                lastseen = :lastSeen
                """)
                    .bind("uuid", player.getUniqueId().toString())
                    .bind("name", player.getName())
                    .bind("lastSeen", player.getLastSeenTimestamp())
                    .execute();

            if (player instanceof MSPlayerProfile onlinePlayer) {
                saveOnlinePlayerData(handle, onlinePlayer);
            }
        });
    }

    private void saveOnlinePlayerData(Handle handle, MSPlayerProfile player) {
        // Save options
        if (player.getOptions() != null) {
            for (Map.Entry<Option, Boolean> entry : player.getOptions().entrySet()) {
                if (entry.getKey().getDefaultValue() != entry.getValue()) {
                    handle.createUpdate("""
                        INSERT INTO sc_options (id, idOption) 
                        VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid), :option) 
                        ON DUPLICATE KEY UPDATE id = VALUES(id)
                        """)
                            .bind("uuid", player.getUniqueId().toString())
                            .bind("option", entry.getKey().toString())
                            .execute();
                }
            }
        }

        // Save ignored players
        if (player.getIgnoredPlayers() != null) {
            for (UUID ignored : player.getIgnoredPlayers()) {
                handle.createUpdate("""
                    INSERT INTO sc_ignored_players (id, uuidPlayer) 
                    VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid), :ignored) 
                    ON DUPLICATE KEY UPDATE id = VALUES(id)
                    """)
                        .bind("uuid", player.getUniqueId().toString())
                        .bind("ignored", ignored.toString())
                        .execute();
            }
        }

        // Save blacklisted announces
        if (player.getBlackListedAnnounces() != null) {
            for (Integer announce : player.getBlackListedAnnounces()) {
                handle.createUpdate("""
                    INSERT INTO sc_annonces_blacklist (id, idAnnonce) 
                    VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid), :announce) 
                    ON DUPLICATE KEY UPDATE id = VALUES(id)
                    """)
                        .bind("uuid", player.getUniqueId().toString())
                        .bind("announce", announce)
                        .execute();
            }
        }

        // Save login
        if (player.getIp() != null) {
            handle.createUpdate("""
                INSERT INTO sc_logins (id, date_time, ip) 
                VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid), :timestamp, :ip)
                """)
                    .bind("uuid", player.getUniqueId().toString())
                    .bind("timestamp", new Timestamp(player.getLastSeenTimestamp()))
                    .bind("ip", player.getIp())
                    .execute();
        }
    }

    public void removeIgnoredPlayer(UUID playerUUID, UUID ignoredUUID) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("""
                DELETE FROM sc_ignored_players 
                WHERE id = (SELECT id FROM sc_players WHERE uuid = :playerUUID) 
                AND uuidPlayer = :ignoredUUID
                """)
                    .bind("playerUUID", playerUUID.toString())
                    .bind("ignoredUUID", ignoredUUID.toString())
                    .execute();
        });
    }
    public Set<UUID> getOnlinePlayerIds() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
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

    public void addOnlinePlayer(UUID uuid) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            INSERT INTO sc_online_players (player_id) 
            VALUES ((SELECT id FROM sc_players WHERE uuid = :uuid)) 
            ON DUPLICATE KEY UPDATE player_id = player_id
            """)
                        .bind("uuid", uuid.toString())
                        .execute()
        );
    }

    public void removeOnlinePlayer(UUID uuid) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            DELETE FROM sc_online_players 
            WHERE player_id = (SELECT id FROM sc_players WHERE uuid = :uuid)
            """)
                        .bind("uuid", uuid.toString())
                        .execute()
        );
    }

    public void clearOnlinePlayers() {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM sc_online_players").execute()
        );
    }
}