package fr.iban.common.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbTables {

    public static void createTables() {
        createplayersTable();
        createLoginsTable();
        createOnlinePlayersTable();
        createTrustedPlayersTable();
        createTrustedCommandsTable();
        createBackupsTable();
    }

    /*
     * Création de la table
     */
    private static void createplayersTable() {
        createTable("CREATE TABLE IF NOT EXISTS sc_players (" +
                "  id          int auto_increment PRIMARY KEY," +
                "  uuid    varchar(36)  not null," +
                "  name        varchar(16)  not null," +
                "  date_created timestamp default now()," +
                "  lastseen       bigint DEFAULT 0," +
                "  CONSTRAINT  UC_sc_players" +
                "  UNIQUE (id)," +
                "  CONSTRAINT UC_sc_players_uuid" +
                "  UNIQUE (uuid)" +
                ");");
    }

    private static void createLoginsTable() {
        createTable("CREATE TABLE IF NOT EXISTS sc_logins (" +
                "  id int," +
                "  date_time DATETIME," +
                "  ip VARCHAR(45)," +
                "  CONSTRAINT PK_sc_logins" +
                "  PRIMARY KEY (id, date_time)," +
                "  CONSTRAINT FK_sc_logins" +
                "  FOREIGN KEY (id) REFERENCES sc_players(id)" +
                ");");
    }

    private static void createOnlinePlayersTable() {
        createTable("CREATE TABLE IF NOT EXISTS sc_online_players (" +
                "  player_id INTEGER PRIMARY KEY," +
                "  FOREIGN KEY (player_id) REFERENCES sc_players(id)" +
                ");");
    }

    private static void createTrustedPlayersTable() {
        createTable("CREATE TABLE IF NOT EXISTS sc_trusted_players (" +
                "  uuid varchar(36)," +
                "  ip VARCHAR(45)," +
                "  date_time DATETIME DEFAULT NOW()," +
                "  PRIMARY KEY (uuid, ip)" +
                ");");
    }

    private static void createTrustedCommandsTable() {
        createTable("CREATE TABLE IF NOT EXISTS sc_trusted_commands (" +
                "  command VARCHAR(255)," +
                "  senderType VARCHAR(50)," +
                "  `context` VARCHAR(50)," +
                "  PRIMARY KEY (command, senderType, context)" +
                ");");
    }

    private static void createBackupsTable() {
        createTable("""
                CREATE TABLE IF NOT EXISTS sc_players_backup (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    data JSON NOT NULL,
                    valid BOOLEAN DEFAULT TRUE,
                    error TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (player_uuid) REFERENCES sc_players(uuid) ON DELETE CASCADE,
                    INDEX idx_player_backup (player_uuid, valid, created_at)
                );
                """);
    }

    private static void createTable(String statement) {
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatemente = connection.prepareStatement(statement)) {
                preparedStatemente.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
