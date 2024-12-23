package fr.iban.common.data.migrations;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.iban.common.data.Migration;
import org.jdbi.v3.core.Jdbi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Migration_1_InitialMigration extends Migration {

    public Migration_1_InitialMigration(Jdbi jdbi) {
            super(jdbi, 1);
    }

    @Override
    public void migrate() {
        jdbi.useTransaction(handle -> {
            // 1. Ajouter la colonne data à sc_players
            handle.execute("ALTER TABLE sc_players ADD COLUMN IF NOT EXISTS data JSON");

            // 2. Migrer les données des tables annexes vers JSON
            List<Map<String, Object>> players = handle.createQuery("SELECT id, uuid FROM sc_players")
                    .mapToMap()
                    .list();

            for (Map<String, Object> player : players) {
                int id = ((Number) player.get("id")).intValue();
                JsonObject data = new JsonObject();

                // 2.1 Migrer les options
                Set<String> options = handle.createQuery(
                                "SELECT idOption FROM sc_options WHERE id = :id")
                        .bind("id", id)
                        .mapTo(String.class)
                        .collect(Collectors.toSet());

                Map<String, Boolean> optionsMap = new HashMap<>();
                for (String option : options) {
                    optionsMap.put(option, true);
                }
                data.add("options", new Gson().toJsonTree(optionsMap));

                // 2.2 Migrer les annonces blacklistées
                Set<Integer> blacklistedAnnounces = handle.createQuery(
                                "SELECT idAnnonce FROM sc_annonces_blacklist WHERE id = :id")
                        .bind("id", id)
                        .mapTo(Integer.class)
                        .collect(Collectors.toSet());
                data.add("blackListedAnnounces", new Gson().toJsonTree(blacklistedAnnounces));

                // 2.3 Migrer les joueurs ignorés
                Set<String> ignoredPlayers = handle.createQuery(
                                "SELECT uuidPlayer FROM sc_ignored_players WHERE id = :id")
                        .bind("id", id)
                        .mapTo(String.class)
                        .collect(Collectors.toSet());
                data.add("ignoredPlayers", new Gson().toJsonTree(ignoredPlayers));

                // 3. Mettre à jour sc_players avec les données JSON
                handle.createUpdate("UPDATE sc_players SET data = :data WHERE id = :id")
                        .bind("id", id)
                        .bind("data", data.toString())
                        .execute();
            }

            // 4. Supprimer les colonnes inutiles de sc_players
            handle.execute("ALTER TABLE sc_players DROP COLUMN IF EXISTS date_created");

            // 5. Supprimer les tables annexes
            handle.execute("DROP TABLE IF EXISTS sc_ignored_players");
            handle.execute("DROP TABLE IF EXISTS sc_annonces_blacklist");
            handle.execute("DROP TABLE IF EXISTS sc_options");

            // 6. Nettoyer les contraintes inutiles
            handle.execute("ALTER TABLE sc_players DROP CONSTRAINT IF EXISTS UC_sc_players");

            // 7. Restructurer sc_players dans sa forme finale
            handle.execute("""
                ALTER TABLE sc_players 
                MODIFY COLUMN id INT AUTO_INCREMENT,
                MODIFY COLUMN uuid VARCHAR(36) NOT NULL,
                MODIFY COLUMN name VARCHAR(16) NOT NULL,
                MODIFY COLUMN lastseen BIGINT DEFAULT 0,
                MODIFY COLUMN data JSON,
                ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            """);
        });
    }
}