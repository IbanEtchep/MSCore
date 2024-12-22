package fr.iban.bukkitcore.rewards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import fr.iban.common.data.sql.DbAccess;

public class RewardsDAO {
	
	public static CompletableFuture<List<Reward>> getRewardsAsync(UUID uuid) {
		return future(() -> {
			List<Reward> rewards = new ArrayList<>();
			String sql = "SELECT * FROM sc_rewards WHERE uuid like ?;";
			try (Connection connection = DbAccess.getDataSource().getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(sql)){
					ps.setString(1, uuid.toString());
					try(ResultSet rs = ps.executeQuery()){
						while(rs.next()) {
							int id = rs.getInt("idR");
							String server = rs.getString("server");
							String libelle = rs.getString("libelle");
							String command = rs.getString("command");
							rewards.add(new Reward(id, libelle, server, command));
						}
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}		
			return rewards;
		});
	}
	
	public static CompletableFuture<List<Reward>> getTemplateRewardsAsync() {
		return future(() -> {
			List<Reward> rewards = new ArrayList<>();
			String sql = "SELECT * FROM sc_rewards WHERE uuid like ?;";
			try (Connection connection = DbAccess.getDataSource().getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(sql)){
					ps.setString(1, "template");
					try(ResultSet rs = ps.executeQuery()){
						while(rs.next()) {
							int id = rs.getInt("idR");
							String server = rs.getString("server");
							String libelle = rs.getString("libelle");
							String command = rs.getString("command");
							rewards.add(new Reward(id, libelle, server, command));
						}
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}		
			return rewards;
		});
	}
	
	public static CompletableFuture<Void> addRewardAsync(String uuid, String libelle, String server, String command) {
		return future(() -> {
			String sql = "INSERT INTO sc_rewards (uuid, libelle, server, command) VALUES(?, ?, ?, ?);";
			try (Connection connection = DbAccess.getDataSource().getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(sql)){
					ps.setString(1, uuid);
					ps.setString(2, libelle);
					ps.setString(3, server);
					ps.setString(4, command);
					ps.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static CompletableFuture<Void> removeRewardAsync(String uuid, Reward reward) {
		return future(() -> {
			String sql = "DELETE FROM sc_rewards WHERE idR=?";
			try (Connection connection = DbAccess.getDataSource().getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(sql)){
					ps.setInt(1, reward.id());
					ps.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void createTables() {
		createTable("CREATE TABLE IF NOT EXISTS sc_rewards(" + 
				"    idR INT AUTO_INCREMENT," + 
				"    uuid VARCHAR(36),"	 +
				"    libelle VARCHAR(255) NOT NULL," + 
				"    server VARCHAR(255) NOT NULL," + 
				"    command VARCHAR(255) NOT NULL," + 
				"    PRIMARY KEY (idR)" +
				");");
	}

	
	private static void createTable(String statement) {
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static <T> CompletableFuture<T> future(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	private static CompletableFuture<Void> future(Runnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
                throw (RuntimeException) e;
            }
		});
	}
}
