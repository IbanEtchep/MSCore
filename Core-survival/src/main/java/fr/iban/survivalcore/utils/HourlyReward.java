package fr.iban.survivalcore.utils;

import fr.iban.bukkitcore.rewards.RewardsDAO;
import fr.iban.common.data.sql.DbAccess;
import fr.iban.survivalcore.SurvivalCorePlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HourlyReward {

    private final SurvivalCorePlugin plugin;

    public HourlyReward(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String createStatement = "CREATE TABLE IF NOT EXISTS sc_hourly_rewards_time (" +
                "uuid VARCHAR(36) PRIMARY KEY , playtime INTEGER);";
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        plugin.getScheduler().runTimerAsync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getSalary(player) != 0) {
                    addPlayTime(player.getUniqueId(), 1);
                }
            }
        }, 1200L, 1200L);

        plugin.getScheduler().runTimerAsync(() -> getTimeBiggerThan(getTimePerPayout()).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                int salary = getSalary(player);
                if(salary == 0) return;
                Economy economy = plugin.getEconomy();
                if (economy != null) {
                    player.sendMessage("§a§lVous avez reçu "+economy.format(salary)+"§a§l pour avoir joué 1 heure.");
                    economy.depositPlayer(player, salary);
                } else {
                    player.sendMessage("§a§lLa récompense vous attend dans /recompenses.");
                    RewardsDAO.addRewardAsync(player.getUniqueId().toString(), salary+"$§r", "Survie", "eco give {player} " + salary);
                }
                removePlayTime(uuid, 60);
            }
        }), 0L, 12000L);
    }


    public List<UUID> getTimeBiggerThan(int time) {
        String sql = "SELECT uuid FROM sc_hourly_rewards_time WHERE playtime > ?;";
        List<UUID> uuidList = new ArrayList<>();
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, time);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        uuidList.add(uuid);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuidList;
    }

    public void addPlayTime(UUID uuid, int minutes) {
        String sql = "INSERT INTO sc_hourly_rewards_time (uuid, playtime) VALUES (?, ?) ON DUPLICATE KEY UPDATE playtime=(playtime+VALUES(playtime));";
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, minutes);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePlayTime(UUID uuid, int minutes) {
        String sql = "UPDATE sc_hourly_rewards_time SET playtime=(playtime-?) WHERE uuid=?;";
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, minutes);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSalary(Player player) {
        int total = 0;
        List<String> salaries = plugin.getConfig().getStringList("salary.permissions");
        for (String salaryString : salaries) {
            String[] splitted = salaryString.split(":");
            String permission = splitted[0];
            int salary = Integer.parseInt(splitted[1]);
            if(player.hasPermission(permission)) {
                total += salary;
            }
        }
        return total;
    }

    public int getTimePerPayout() {
        return plugin.getConfig().getInt("salary.time-per-payout", 60);
    }
}
