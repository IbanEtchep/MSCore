package fr.iban.common.data;

import fr.iban.common.data.migrations.Migration_1_InitialMigration;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MigrationManager {

    private final Logger logger;
    private final Jdbi jdbi;
    private final List<Migration> migrations = new ArrayList<>();
    private static final String VERSION_TABLE = "sc_schema_version";

    public MigrationManager(Jdbi jdbi, Logger logger) {
        this.jdbi = jdbi;
        this.logger = logger;
        migrations.add(new Migration_1_InitialMigration(jdbi));
    }

    public void initialize() {
        createVersionTableIfNeeded();
        int currentVersion = getCurrentVersion();

        migrations.stream()
                .filter(m -> m.getVersion() > currentVersion)
                .forEach(this::executeMigration);
    }

    private void executeMigration(Migration migration) {
        try {
            jdbi.useTransaction(handle -> {
                migration.migrate();
                updateVersion(migration.getVersion());
            });
            logger.info("Migration {} completed successfully", migration.getVersion());
        } catch (Exception e) {
            logger.error("Migration {} failed", migration.getVersion(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    private void createVersionTableIfNeeded() {
        jdbi.useHandle(handle -> {
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS " + VERSION_TABLE + " (" +
                            "version INT NOT NULL DEFAULT 0" +
                            ")"
            );

            // Insert initial version if table is empty
            int count = handle.createQuery("SELECT COUNT(*) FROM " + VERSION_TABLE)
                    .mapTo(Integer.class)
                    .one();

            if (count == 0) {
                handle.execute("INSERT INTO " + VERSION_TABLE + " (version) VALUES (0)");
            }
        });
    }

    private int getCurrentVersion() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT version FROM " + VERSION_TABLE)
                        .mapTo(Integer.class)
                        .one()
        );
    }

    private void updateVersion(int version) {
        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE " + VERSION_TABLE + " SET version = :version")
                        .bind("version", version)
                        .execute()
        );
    }
}