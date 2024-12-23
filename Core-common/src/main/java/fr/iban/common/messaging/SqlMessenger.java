package fr.iban.common.messaging;

import fr.iban.common.data.sql.DbAccess;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.concurrent.*;

public class SqlMessenger extends AbstractMessenger {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DataSource ds = DbAccess.getDataSource();
    private boolean locked = false;
    private long lastId;

    public void init() {
        executor.execute(() -> {
            try (Connection connection = ds.getConnection()) {

                String createStatement = "CREATE TABLE IF NOT EXISTS sc_messenger(" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "createdAt TIMESTAMP NOT NULL," +
                        "serverFrom VARCHAR(255)," +
                        "channel VARCHAR(255)," +
                        "msg TEXT NOT NULL" +
                        ");";

                try (PreparedStatement preparedStatement = connection.prepareStatement(createStatement)) {
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement("SELECT MAX(`id`) as latest FROM sc_messenger;")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            this.lastId = rs.getLong("latest");
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            scheduler.scheduleAtFixedRate(this::readNewMessages, 10L, 10L, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(this::cleanOldMessages, 30L, 30L, TimeUnit.SECONDS);
        });
    }

    @Override
    public void close() {
        scheduler.shutdown();
        executor.shutdown();
    }

    public void sendMessage(Message message) {
        executor.execute(() -> {
            long now = System.nanoTime();
            String insertStatement = "INSERT INTO sc_messenger (createdAt, serverFrom, channel, msg) VALUES (NOW(), ?, ?, ?);";
            try (Connection connection = ds.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement(insertStatement)) {
                    ps.setString(1, message.getServerFrom());
                    ps.setString(2, message.getChannel());
                    ps.setString(3, message.getMessage());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(debugMode) {
                System.out.println("OUT : " + "("+ message.getChannel() +") " + message.getMessage() + " envoyé à " + System.currentTimeMillis() + " en " + (System.nanoTime()-now)/1000000.0 +"ms");
            }
        });
    }

    public void readNewMessages() {
        if (locked) {
            if(debugMode) {
                System.out.println("readNewMessages() locked");
            }
            return;
        }else locked = true;
        String selectStatement = "SELECT id, serverFrom, channel, msg FROM sc_messenger WHERE id > ? AND (NOW() - createdAt < 15) ";
        long now = System.nanoTime();

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(selectStatement)) {
                ps.setLong(1, this.lastId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        this.lastId = Math.max(this.lastId, id);
                        String channel = rs.getString("channel");
                        String serverFrom = rs.getString("serverFrom");
                        String message = rs.getString("msg");
                        messageConsumer.accept(new Message(channel, serverFrom, message));
                        if(debugMode) {
                            System.out.println("IN : " + "("+ channel +") " + message + " lu à " + System.currentTimeMillis() + " en " + (System.nanoTime()-now)/1000000.0 +"ms");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.locked = false;
        }
    }

    public void cleanOldMessages() {
        String deleteStatement = "DELETE FROM sc_messenger WHERE (NOW() - createdAt > 30)";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(deleteStatement)) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
