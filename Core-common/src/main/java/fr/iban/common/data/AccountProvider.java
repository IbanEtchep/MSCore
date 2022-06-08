package fr.iban.common.data;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import fr.iban.common.data.redis.RedisAccess;
import fr.iban.common.data.sql.DbAccess;

public class AccountProvider {

	public static final String REDIS_KEY = "account:";

	private UUID uuid;
	private RedisAccess redisAccess;
	private boolean hasPlayedBefore = true;

	public AccountProvider(UUID uuid) {
		this.uuid = uuid;
		this.redisAccess = RedisAccess.getInstance();
	}

	public Account getAccount(){
		Account account = getAccountFromRedis();

		if(account == null) {
			account = getAccountFromDB();
			sendAccountToRedis(account);

		}

		return account;
	}

	public CompletableFuture<Account> getAccountAsync() {
		return CompletableFuture.supplyAsync(this::getAccount);
	}

	private Account getAccountFromDB(){
		DataSource ds = DbAccess.getDataSource();
		Account account = new Account(uuid);

		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM sc_players WHERE uuid = ?")){
				ps.setString(1, uuid.toString());
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) {
						long lastseen = rs.getLong("lastseen");
						account.setLastSeen(lastseen);

						short maxclaims = rs.getShort("maxclaims");
						account.setMaxClaims(maxclaims);
					}else {
						hasPlayedBefore = false;
					}
				}
			}
			account.setOptions(getOptionsFromDB(connection));
			account.setIgnoredPlayers(getIgnoredPlayersFromDB(connection));
			account.setBlackListedAnnounces(getBlackListedAnnouncesFromDB(connection));
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return account;
	}

	public void sendAccountToDB(Account account) {
		try (Connection connection = DbAccess.getDataSource().getConnection()){
			try(PreparedStatement ps = connection.prepareStatement("INSERT INTO sc_players (uuid, name, lastseen, maxclaims) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), lastseen=VALUES(lastseen), maxclaims=VALUES(maxclaims)")){
				ps.setString(1, uuid.toString());
				ps.setString(2, (account.getName() == null ? "NonDefini" : account.getName()));
				ps.setLong(3, account.getLastSeen());
				ps.setInt(4, account.getMaxClaims());
				ps.executeUpdate();
			}
			saveOptionsToDB(account.getOptions(), connection);
			deleteOptionsFromDB(account.getOptions(), connection);
			saveBlackListedAnnouncesToDB(account.getBlackListedAnnounces(), connection);
			if(account.getIp() != null) {
				saveLoginToDb(account, connection);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Account getAccountFromRedis() {
		RedissonClient client = redisAccess.getRedissonClient();
		String key = REDIS_KEY+uuid.toString();
		RBucket<Account> accountBukket = client.getBucket(key);
		return accountBukket.get();
	}

	public void sendAccountToRedis(Account account) {
		RedissonClient client = redisAccess.getRedissonClient();
		String key = REDIS_KEY+uuid.toString();
		RBucket<Account> accountBukket = client.getBucket(key);
		accountBukket.set(account);
	}

	public void removeAccountFromRedis() {
		RedissonClient client = redisAccess.getRedissonClient();
		String key = REDIS_KEY+uuid.toString();
		RBucket<Account> accountBukket = client.getBucket(key);
		accountBukket.delete();
	}

	public Map<Option, Boolean> getOptionsFromDB(Connection connection) throws SQLException{
		Map<Option, Boolean> options = new HashMap<>();
		try(PreparedStatement ps = 
				connection.prepareStatement(
						"SELECT idOption "
								+ "FROM sc_players, sc_options "
								+ "WHERE uuid = ? "
								+ "AND sc_players.id=sc_options.id"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					Option option = Option.valueOf(rs.getString("idOption"));
					options.put(option, !option.getDefaultValue());
				}
			}
		}
		return options;
	}

	public Set<Integer> getBlackListedAnnouncesFromDB(Connection connection) throws SQLException{
		Set<Integer> announces = new HashSet<>();
		try(PreparedStatement ps = 
				connection.prepareStatement(
						"SELECT idAnnonce "
								+ "FROM sc_players, sc_annonces_blacklist "
								+ "WHERE uuid = ? "
								+ "AND sc_players.id=sc_annonces_blacklist.id"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					announces.add(rs.getInt("idAnnonce"));
				}
			}
		}
		return announces;
	}

	public Set<UUID> getIgnoredPlayersFromDB(Connection connection) throws SQLException{
		Set<UUID> ignored = new HashSet<>();
		try(PreparedStatement ps = 
				connection.prepareStatement(
						"SELECT uuidPlayer "
								+ "FROM sc_players, sc_ignored_players "
								+ "WHERE uuid = ? "
								+ "AND sc_players.id=sc_ignored_players.id"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					ignored.add(UUID.fromString(rs.getString("uuidPlayer")));
				}
			}
		}
		return ignored;
	}

	public void saveIgnoredPlayersToDB(Set<UUID> ignored, Connection connection) throws SQLException {
		final String INSERT_SQL = "INSERT INTO sc_ignored_players(id, uuidPlayer) VALUES ((SELECT id FROM sc_players WHERE uuid=?), ?) ON DUPLICATE KEY UPDATE id=VALUES(id);";
		for(UUID uuidP : ignored) {
			PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
			ps.setString(1, uuid.toString());
			ps.setString(2, uuidP.toString());
			ps.executeUpdate();
			ps.close();
		}
	}

	public void saveOptionsToDB(Map<Option, Boolean> options, Connection connection) throws SQLException {
		final String INSERT_SQL = "INSERT INTO sc_options(id, idOption) VALUES ((SELECT id FROM sc_players WHERE uuid=?), ?) ON DUPLICATE KEY UPDATE id=VALUES(id);";
		for(Entry<Option, Boolean> entry : options.entrySet()) {
			Option option = entry.getKey();
			if(option.getDefaultValue() == entry.getValue().booleanValue()) continue;
			PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
			ps.setString(1, uuid.toString());
			ps.setString(2, option.toString());
			ps.executeUpdate();
			ps.close();
		}
	}

	public void deleteOptionsFromDB(Map<Option, Boolean> options, Connection connection) throws SQLException {
		final String DELETE_SQL = "DELETE FROM sc_options WHERE id = (SELECT id FROM sc_players WHERE uuid=?) AND idOption = ?;";
		for(Map.Entry<Option, Boolean> entry : options.entrySet()) {
			Option option = entry.getKey();
			if(option.getDefaultValue() != entry.getValue().booleanValue()) continue;
			PreparedStatement ps = connection.prepareStatement(DELETE_SQL);
			ps.setString(1, uuid.toString());
			ps.setString(2, option.toString());
			ps.executeUpdate();
			ps.close();
		}
	}

	public void saveBlackListedAnnouncesToDB(Set<Integer> blacklist, Connection connection) throws SQLException {
		final String INSERT_SQL = "INSERT INTO sc_annonces_blacklist(id, idAnnonce) VALUES ((SELECT id FROM sc_players WHERE uuid=?), ?) ON DUPLICATE KEY UPDATE id=VALUES(id);";
		for(int idA : blacklist) {
			PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
			ps.setString(1, uuid.toString());
			ps.setInt(2, idA);
			ps.executeUpdate();
			ps.close();
		}
	}

	public void saveLoginToDb(Account account, Connection connection) throws SQLException {
		final String INSERT_SQL = "INSERT INTO sc_logins(id, date_time, ip) VALUES ((SELECT id FROM sc_players WHERE uuid=?), ?, ?) ON DUPLICATE KEY UPDATE id=VALUES(id);";
		PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
		ps.setString(1, uuid.toString());
		ps.setTimestamp(2, new Timestamp(account.getLastSeen()));
		ps.setString(3, account.getIp());
		ps.executeUpdate();
		ps.close();
	}

	public boolean hasPlayedBefore() {
		return hasPlayedBefore;
	}
}
