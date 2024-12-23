package fr.iban.common.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.iban.common.data.MigrationManager;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DbAccess {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbAccess.class);
	private static HikariDataSource dataSource;
	private static Jdbi jdbi;

	private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(5, r -> {
		Thread thread = new Thread(r, "DB-Thread");
		thread.setDaemon(true);
		return thread;
	});

	public static void initPool(DbCredentials credentials) {
		try {
			HikariConfig config = new HikariConfig();

			// Configuration basique
			config.setDriverClassName("com.mysql.cj.jdbc.Driver");
			config.setJdbcUrl(credentials.toURI());
			config.setUsername(credentials.getUser());
			config.setPassword(credentials.getPass());

			// Configuration de la pool
			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);
			config.setIdleTimeout(300000); // 5 minutes
			config.setMaxLifetime(600000); // 10 minutes
			config.setConnectionTimeout(10000); // 10 secondes

			// Optimisations MySQL
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			config.addDataSourceProperty("useLocalSessionState", "true");
			config.addDataSourceProperty("rewriteBatchedStatements", "true");
			config.addDataSourceProperty("cacheResultSetMetadata", "true");
			config.addDataSourceProperty("cacheServerConfiguration", "true");
			config.addDataSourceProperty("elideSetAutoCommits", "true");
			config.addDataSourceProperty("maintainTimeStats", "false");

			LOGGER.info("Initializing database connection to {}", credentials.toURI());
			dataSource = new HikariDataSource(config);

			// Initialize JDBI
			jdbi = Jdbi.create(dataSource);

			// Test connection
			if (testConnection()) {
				LOGGER.info("Database connection established successfully");

				// Exécuter les migrations
				LOGGER.info("Running database migrations...");
				MigrationManager migrationManager = new MigrationManager(jdbi, LOGGER);
				migrationManager.initialize();
				LOGGER.info("Database migrations completed");
			}

		} catch (Exception e) {
			LOGGER.error("Failed to initialize database connection", e);
			throw new RuntimeException("Database initialization failed", e);
		}
	}

	public static void closePool() {
		if (dataSource != null && !dataSource.isClosed()) {
			LOGGER.info("Closing database connection pool");
			dataSource.close();
		}

		DB_EXECUTOR.shutdown();
	}

	public static DataSource getDataSource() {
		if (dataSource == null || dataSource.isClosed()) {
			throw new IllegalStateException("Database connection pool is not initialized");
		}
		return dataSource;
	}

	public static Jdbi getJdbi() {
		if (jdbi == null) {
			throw new IllegalStateException("JDBI is not initialized");
		}
		return jdbi;
	}

	private static boolean testConnection() {
		try (Connection conn = dataSource.getConnection()) {
			return conn.isValid(5);
		} catch (SQLException e) {
			LOGGER.error("Database connection test failed", e);
			return false;
		}
	}
}