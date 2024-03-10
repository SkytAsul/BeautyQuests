package fr.skytasul.quests.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.ThrowingConsumer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PlayersManagerDB extends AbstractPlayersManager {

	public final String ACCOUNTS_TABLE;
	public final String QUESTS_DATAS_TABLE;
	public final String POOLS_DATAS_TABLE;

	private final Database db;

	private final Map<SavableData<?>, SQLDataSaver<?>> accountDatas = new HashMap<>();
	private String getAccountDatas;
	private String resetAccountDatas;

	/* Accounts statements */
	private String getAccountsIDs;
	private String insertAccount;
	private String deleteAccount;

	/* Quest datas statements */
	private String insertQuestData;
	private String removeQuestData;
	private String getQuestsData;

	private String removeExistingQuestDatas;

	private String updateFinished;
	private String updateTimer;
	private String updateBranch;
	private String updateStage;
	private String updateDatas;
	private String updateFlow;

	/* Pool datas statements */
	private String insertPoolData;
	private String removePoolData;
	private String getPoolData;
	private String getPoolAccountData;

	private String updatePoolLastGive;
	private String updatePoolCompletedQuests;

	public PlayersManagerDB(Database db) {
		this.db = db;
		ACCOUNTS_TABLE = db.getConfig().getString("tables.playerAccounts");
		QUESTS_DATAS_TABLE = db.getConfig().getString("tables.playerQuests");
		POOLS_DATAS_TABLE = db.getConfig().getString("tables.playerPools");
	}

	public Database getDatabase() {
		return db;
	}

	@Override
	public void addAccountData(SavableData<?> data) {
		super.addAccountData(data);
		accountDatas.put(data,
				new SQLDataSaver<>(data, "UPDATE " + ACCOUNTS_TABLE + " SET " + data.getColumnName() + " = ? WHERE id = ?"));
		getAccountDatas = accountDatas.keySet()
				.stream()
				.map(SavableData::getColumnName)
				.collect(Collectors.joining(", ", "SELECT ", " FROM " + ACCOUNTS_TABLE + " WHERE id = ?"));
		resetAccountDatas = accountDatas.values()
				.stream()
				.map(x -> x.getWrappedData().getColumnName() + " = " + x.getDefaultValueString())
				.collect(Collectors.joining(", ", "UPDATE " + ACCOUNTS_TABLE + " SET ", " WHERE id = ?"));
	}

	private void retrievePlayerDatas(PlayerAccountImplementation acc) {
		try (Connection connection = db.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(getQuestsData)) {
				statement.setInt(1, acc.index);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					int questID = result.getInt("quest_id");
					acc.questDatas.put(questID, new PlayerQuestDatasDB(acc, questID, result));
				}
				result.close();
			}
			try (PreparedStatement statement = connection.prepareStatement(getPoolData)) {
				statement.setInt(1, acc.index);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					int poolID = result.getInt("pool_id");
					String completedQuests = result.getString("completed_quests");
					if (StringUtils.isEmpty(completedQuests)) completedQuests = null;
					acc.poolDatas.put(poolID, new PlayerPoolDatasDB(acc, poolID, result.getLong("last_give"), completedQuests == null ? new HashSet<>() : Arrays.stream(completedQuests.split(";")).map(Integer::parseInt).collect(Collectors.toSet())));
				}
				result.close();
			}
			if (getAccountDatas != null) {
				try (PreparedStatement statement = connection.prepareStatement(getAccountDatas)) {
					statement.setInt(1, acc.index);
					ResultSet result = statement.executeQuery();
					result.next();
					for (SQLDataSaver<?> data : accountDatas.values()) {
						acc.additionalDatas.put(data.getWrappedData(), data.getFromResultSet(result));
					}
					result.close();
				}
			}
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while fetching account datas of " + acc.debugName(), ex);
		}
	}

	@Override
	public void load(AccountFetchRequest request) {
		try (Connection connection = db.getConnection()) {
			String uuid = request.getOfflinePlayer().getUniqueId().toString();
			try (PreparedStatement statement = connection.prepareStatement(getAccountsIDs)) {
				statement.setString(1, uuid);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					AbstractAccount abs = createAccountFromIdentifier(result.getString("identifier"));
					if (abs.isCurrent()) {
						PlayerAccountImplementation account = new PlayerAccountDB(abs, result.getInt("id"));
						result.close();
						try {
							// in order to ensure that, if the player was previously connected to another server,
							// its datas have been fully pushed to database, we wait for 0,4 seconds
							long timeout = 400 - (System.currentTimeMillis() - request.getJoinTimestamp());
							if (timeout > 0)
								Thread.sleep(timeout);
						}catch (InterruptedException e) {
							e.printStackTrace();
							Thread.currentThread().interrupt();
						}
						retrievePlayerDatas(account);
						request.loaded(account, "database");
						return;
					}
				}
			}

			if (request.mustCreateMissing()) {
				try (PreparedStatement statement =
						connection.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS)) {
					AbstractAccount absacc = super.createAbstractAccount(request.getOnlinePlayer());
					statement.setString(1, absacc.getIdentifier());
					statement.setString(2, uuid);
					statement.executeUpdate();
					ResultSet result = statement.getGeneratedKeys();
					if (!result.next())
						throw new SQLException("The plugin has not been able to create a player account.");
					int index = result.getInt(1); // some drivers don't return a ResultSet with correct column names
					request.created(new PlayerAccountDB(absacc, index));
				}
			} else {
				request.notLoaded();
			}
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while loading account of " + request.getDebugPlayerName(), ex);
		}
	}

	@Override
	protected CompletableFuture<Void> removeAccount(PlayerAccountImplementation acc) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(deleteAccount)) {
				statement.setInt(1, acc.index);
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing account from database.", ex);
			}
		});
	}

	@Override
	public PlayerQuestDatasImplementation createPlayerQuestDatas(PlayerAccountImplementation acc, Quest quest) {
		return new PlayerQuestDatasDB(acc, quest.getId());
	}

	@Override
	public CompletableFuture<Void> playerQuestDataRemoved(PlayerQuestDatasImplementation datas) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(removeQuestData)) {
				((PlayerQuestDatasDB) datas).stop();
				statement.setInt(1, datas.acc.index);
				statement.setInt(2, datas.questID);
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing player quest data from database.", ex);
			}
		});
	}

	@Override
	public PlayerPoolDatasImplementation createPlayerPoolDatas(PlayerAccountImplementation acc, QuestPool pool) {
		return new PlayerPoolDatasDB(acc, pool.getId());
	}

	@Override
	public CompletableFuture<Void> playerPoolDataRemoved(PlayerPoolDatasImplementation datas) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(removePoolData)) {
				statement.setInt(1, datas.acc.index);
				statement.setInt(2, datas.poolID);
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing player quest data from database.", ex);
			}
		});
	}

	@Override
	public CompletableFuture<Integer> removeQuestDatas(Quest quest) {
		return CompletableFuture.supplyAsync(() -> {
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(removeExistingQuestDatas)) {
				for (PlayerAccountImplementation acc : cachedAccounts.values()) {
					PlayerQuestDatasDB datas = (PlayerQuestDatasDB) acc.removeQuestDatasSilently(quest.getId());
					if (datas != null) datas.stop();
				}
				statement.setInt(1, quest.getId());
				int amount = statement.executeUpdate();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Removed " + amount + " in-database quest datas for quest " + quest.getId());
				return amount;
			} catch (SQLException ex) {
				throw new DataException("Failed to remove quest datas from database.", ex);
			}
		});
	}

	public CompletableFuture<Boolean> hasAccounts(Player p) {
		return CompletableFuture.supplyAsync(() -> {
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(getAccountsIDs)) {
				statement.setString(1, p.getUniqueId().toString());
				ResultSet result = statement.executeQuery();
				boolean has = result.next();
				result.close();
				return has;
			} catch (SQLException ex) {
				throw new DataException("An error occurred while fetching account from database.", ex);
			}
		});
	}

	@Override
	public void load() {
		super.load();
		try {
			createTables();

			getAccountsIDs = "SELECT id, identifier FROM " + ACCOUNTS_TABLE + " WHERE player_uuid = ?";
			insertAccount = "INSERT INTO " + ACCOUNTS_TABLE + " (identifier, player_uuid) VALUES (?, ?)";
			deleteAccount = "DELETE FROM " + ACCOUNTS_TABLE + " WHERE id = ?";

			insertQuestData = "INSERT INTO " + QUESTS_DATAS_TABLE + " (account_id, quest_id) VALUES (?, ?)";
			removeQuestData = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE account_id = ? AND quest_id = ?";
			getQuestsData = "SELECT * FROM " + QUESTS_DATAS_TABLE + " WHERE account_id = ?";

			removeExistingQuestDatas = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE quest_id = ?";

			updateFinished = prepareDatasStatement("finished");
			updateTimer = prepareDatasStatement("timer");
			updateBranch = prepareDatasStatement("current_branch");
			updateStage = prepareDatasStatement("current_stage");
			updateDatas = prepareDatasStatement("additional_datas");
			updateFlow = prepareDatasStatement("quest_flow");

			insertPoolData = "INSERT INTO " + POOLS_DATAS_TABLE + " (account_id, pool_id) VALUES (?, ?)";
			removePoolData = "DELETE FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ? AND pool_id = ?";
			getPoolData = "SELECT * FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ?";
			getPoolAccountData = "SELECT 1 FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ? AND pool_id = ?";

			updatePoolLastGive = "UPDATE " + POOLS_DATAS_TABLE + " SET last_give = ? WHERE account_id = ? AND pool_id = ?";
			updatePoolCompletedQuests =
					"UPDATE " + POOLS_DATAS_TABLE + " SET completed_quests = ? WHERE account_id = ? AND pool_id = ?";
		}catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String prepareDatasStatement(String column) throws SQLException {
		return "UPDATE " + QUESTS_DATAS_TABLE + " SET " + column + " = ? WHERE id = ?";
	}

	@Override
	public void save() {
		cachedAccounts.values().forEach(x -> saveAccount(x, false));
	}

	private void createTables() throws SQLException {
		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE + " ("
					+ " id " + db.getType().getSerialType() + " ,"
					+ " identifier TEXT NOT NULL ,"
					+ " player_uuid CHAR(36) NOT NULL ,"
					+ accountDatas.values().stream().map(data -> " " + data.getColumnDefinition() + " ,").collect(Collectors.joining())
					+ " PRIMARY KEY (id)"
					+ " )");
			statement.execute("CREATE TABLE IF NOT EXISTS " + QUESTS_DATAS_TABLE + " (" +
					" id " + db.getType().getSerialType() + " ," +
					" account_id INT NOT NULL," +
					" quest_id INT NOT NULL," +
					" finished INT DEFAULT NULL," +
					" timer BIGINT DEFAULT NULL," +
					" current_branch SMALLINT DEFAULT NULL," +
					" current_stage SMALLINT DEFAULT NULL," +
					" additional_datas " + db.getType().getLongTextType() + " DEFAULT NULL," +
					" quest_flow VARCHAR(8000) DEFAULT NULL," +
					" PRIMARY KEY (id)" +
					")");
			statement.execute("CREATE TABLE IF NOT EXISTS " + POOLS_DATAS_TABLE + " ("
					+ " id " + db.getType().getSerialType() + " ,"
					+ "account_id INT NOT NULL, "
					+ "pool_id INT NOT NULL, "
					+ "last_give BIGINT DEFAULT NULL, "
					+ "completed_quests VARCHAR(1000) DEFAULT NULL, "
					+ "PRIMARY KEY (id)"
					+ ")");

			upgradeTable(connection, QUESTS_DATAS_TABLE, columns -> {
				if (!columns.contains("quest_flow")) { // 0.19
					statement.execute("ALTER TABLE " + QUESTS_DATAS_TABLE
							+ " ADD COLUMN quest_flow VARCHAR(8000) DEFAULT NULL");
					QuestsPlugin.getPlugin().getLoggerExpanded().info("Updated database with quest_flow column.");
				}

				if (!columns.contains("additional_datas") || columns.contains("stage_0_datas")) { // 0.20
					// tests for stage_0_datas: it's in the case the server crashed/stopped during the migration process.
					if (!columns.contains("additional_datas")) {
						statement.execute("ALTER TABLE " + QUESTS_DATAS_TABLE
								+ " ADD COLUMN additional_datas " + db.getType().getLongTextType()
								+ " DEFAULT NULL AFTER current_stage");
						QuestsPlugin.getPlugin().getLoggerExpanded().info("Updated table " + QUESTS_DATAS_TABLE + " with additional_datas column.");
					}

					QuestUtils.runAsync(this::migrateOldQuestDatas);
				}
			});

			upgradeTable(connection, ACCOUNTS_TABLE, columns -> {
				for (SQLDataSaver<?> data : accountDatas.values()) {
					if (!columns.contains(data.getWrappedData().getColumnName().toLowerCase())) {
						statement.execute("ALTER TABLE " + ACCOUNTS_TABLE
								+ " ADD COLUMN " + data.getColumnDefinition());
						QuestsPlugin.getPlugin().getLoggerExpanded().info("Updated database by adding the missing " + data.getWrappedData().getColumnName() + " column in the player accounts table.");
					}
				}
			});
		}
	}

	private void upgradeTable(Connection connection, String tableName, ThrowingConsumer<List<String>, SQLException> columnsConsumer) throws SQLException {
		List<String> columns = new ArrayList<>(14);
		try (ResultSet set = connection.getMetaData().getColumns(db.getDatabase(), null, tableName, null)) {
			while (set.next()) {
				columns.add(set.getString("COLUMN_NAME").toLowerCase());
			}
		}
		if (columns.isEmpty()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot check integrity of SQL table " + tableName);
		}else {
			columnsConsumer.accept(columns);
		}
	}

	private void migrateOldQuestDatas() {
		QuestsPlugin.getPlugin().getLoggerExpanded().info("---- CAUTION ----\n"
				+ "BeautyQuests will now migrate old quest datas in database to the newest format.\n"
				+ "This may take a LONG time. Players should NOT enter the server during this time, "
				+ "or serious data loss can occur.");

		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {

			int deletedDuplicates =
					statement.executeUpdate("DELETE R1 FROM " + QUESTS_DATAS_TABLE + " R1"
							+ " JOIN " + QUESTS_DATAS_TABLE + " R2"
							+ " ON R1.account_id = R2.account_id"
							+ " AND R1.quest_id = R2.quest_id"
							+ " AND R1.id < R2.id;");
			if (deletedDuplicates > 0) QuestsPlugin.getPlugin().getLoggerExpanded().info("Deleted " + deletedDuplicates + " duplicated rows in the " + QUESTS_DATAS_TABLE + " table.");

			int batchCount = 0;
			PreparedStatement migration =
					connection.prepareStatement("UPDATE " + QUESTS_DATAS_TABLE + " SET additional_datas = ? WHERE id = ?");
			ResultSet result = statement.executeQuery(
					"SELECT id, stage_0_datas, stage_1_datas, stage_2_datas, stage_3_datas, stage_4_datas FROM "
							+ QUESTS_DATAS_TABLE);
			while (result.next()) {
				Map<String, Object> datas = new HashMap<>();
				for (int i = 0; i < 5; i++) {
					String stageDatas = result.getString("stage_" + i + "_datas");
					if (stageDatas != null && !"{}".equals(stageDatas)) datas.put("stage" + i, CustomizedObjectTypeAdapter.deserializeNullable(stageDatas, Map.class));
				}

				if (datas.isEmpty()) continue;
				migration.setString(1, CustomizedObjectTypeAdapter.serializeNullable(datas));
				migration.setInt(2, result.getInt("id"));
				migration.addBatch();
				batchCount++;
			}
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Migrating " + batchCount + "quest datas...");
			int migrated = migration.executeBatch().length;
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Migrated " + migrated + " quest datas.");

			statement.execute("ALTER TABLE " + QUESTS_DATAS_TABLE
					+ " DROP COLUMN stage_0_datas,"
					+ " DROP COLUMN stage_1_datas,"
					+ " DROP COLUMN stage_2_datas,"
					+ " DROP COLUMN stage_3_datas,"
					+ " DROP COLUMN stage_4_datas;");
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Updated database by deleting old stage_[0::4]_datas columns.");
			QuestsPlugin.getPlugin().getLoggerExpanded().info("---- CAUTION ----\n"
					+ "The data migration succeeded. Players can now safely connect.");
		}catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("---- CAUTION ----\n"
					+ "The plugin failed to migrate old quest datas in database.", ex);
		}
	}

	public static synchronized String migrate(Database db, PlayersManagerYAML yaml) throws SQLException {
		try (Connection connection = db.getConnection()) {
			PlayersManagerDB manager = new PlayersManagerDB(db);

			ResultSet result = connection.getMetaData().getTables(null, null, "%", null);
			while (result.next()) {
				String tableName = result.getString(3);
				if (tableName.equals(manager.ACCOUNTS_TABLE) || tableName.equals(manager.QUESTS_DATAS_TABLE)
						|| tableName.equals(manager.POOLS_DATAS_TABLE)) {
					result.close();
					return "§cTable \"" + tableName + "\" already exists. Please drop it before migration.";
				}
			}
			result.close();

			manager.createTables();

			PreparedStatement insertAccount =
					connection.prepareStatement(
							"INSERT INTO " + manager.ACCOUNTS_TABLE + " (id, identifier, player_uuid) VALUES (?, ?, ?)");
			PreparedStatement insertQuestData =
					connection.prepareStatement("INSERT INTO " + manager.QUESTS_DATAS_TABLE
							+ " (account_id, quest_id, finished, timer, current_branch, current_stage, additional_datas, quest_flow) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			PreparedStatement insertPoolData =
					connection.prepareStatement("INSERT INTO " + manager.POOLS_DATAS_TABLE
							+ " (account_id, pool_id, last_give, completed_quests) VALUES (?, ?, ?, ?)");

			int amount = 0, failed = 0;
			yaml.loadAllAccounts();
			for (PlayerAccountImplementation acc : yaml.loadedAccounts.values()) {
				try {
					insertAccount.setInt(1, acc.index);
					insertAccount.setString(2, acc.abstractAcc.getIdentifier());
					insertAccount.setString(3, acc.getOfflinePlayer().getUniqueId().toString());
					insertAccount.executeUpdate();

					for (Entry<Integer, PlayerQuestDatasImplementation> entry : acc.questDatas.entrySet()) {
						insertQuestData.setInt(1, acc.index);
						insertQuestData.setInt(2, entry.getKey());
						insertQuestData.setInt(3, entry.getValue().getTimesFinished());
						insertQuestData.setLong(4, entry.getValue().getTimer());
						insertQuestData.setInt(5, entry.getValue().getBranch());
						insertQuestData.setInt(6, entry.getValue().getStage());
						insertQuestData.setString(7, entry.getValue().getRawAdditionalDatas().isEmpty() ? null
								: CustomizedObjectTypeAdapter.serializeNullable(entry.getValue().getRawAdditionalDatas()));
						insertQuestData.setString(8, entry.getValue().getQuestFlow());
						insertQuestData.executeUpdate();
					}

					for (Entry<Integer, PlayerPoolDatasImplementation> entry : acc.poolDatas.entrySet()) {
						insertPoolData.setInt(1, acc.index);
						insertPoolData.setInt(2, entry.getKey());
						insertPoolData.setLong(3, entry.getValue().getLastGive());
						insertPoolData.setString(4, getCompletedQuestsString(entry.getValue().getCompletedQuests()));
						insertPoolData.executeUpdate();
					}

					amount++;
				}catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to migrate datas for account " + acc.debugName(), ex);
					failed++;
				}
			}

			insertAccount.close();
			insertQuestData.close();
			insertPoolData.close();

			return "§aMigration succeed! " + amount + " accounts migrated, " + failed + " accounts failed to migrate.\n§oDatabase saving system is §lnot§r§a§o enabled. You need to reboot the server with the line \"database.enabled\" set to true.";
		}
	}

	@Override
	public void unloadAccount(PlayerAccountImplementation acc) {
		QuestUtils.runAsync(() -> saveAccount(acc, true));
	}

	public void saveAccount(PlayerAccountImplementation acc, boolean stop) {
		acc.getQuestsDatas()
			.stream()
			.map(PlayerQuestDatasDB.class::cast)
				.forEach(x -> x.flushAll(stop));
	}

	protected static String getCompletedQuestsString(Set<Integer> completedQuests) {
		return completedQuests.isEmpty() ? null : completedQuests.stream().map(x -> Integer.toString(x)).collect(Collectors.joining(";"));
	}

	public class PlayerQuestDatasDB extends PlayerQuestDatasImplementation {

		private static final int DATA_QUERY_TIMEOUT = 15;
		private static final int DATA_FLUSHING_TIME = 10;

		private Map<String, Entry<ScheduledFuture<?>, Object>> cachedDatas = new HashMap<>(5);
		private Lock datasLock = new ReentrantLock();
		private Lock dbLock = new ReentrantLock();
		private boolean disabled = false;
		private int dbId = -1;

		public PlayerQuestDatasDB(PlayerAccountImplementation acc, int questID) {
			super(acc, questID);
		}

		public PlayerQuestDatasDB(PlayerAccountImplementation acc, int questID, ResultSet result) throws SQLException {
			super(
					acc,
					questID,
					result.getLong("timer"),
					result.getInt("finished"),
					result.getInt("current_branch"),
					result.getInt("current_stage"),
					CustomizedObjectTypeAdapter.deserializeNullable(result.getString("additional_datas"), Map.class),
					result.getString("quest_flow"));
			this.dbId = result.getInt("id");
		}

		@Override
		public void incrementFinished() {
			super.incrementFinished();
			setDataStatement(updateFinished, getTimesFinished(), false);
		}

		@Override
		public void setTimer(long timer) {
			super.setTimer(timer);
			setDataStatement(updateTimer, timer, false);
		}

		@Override
		public void setBranch(int branch) {
			super.setBranch(branch);
			setDataStatement(updateBranch, branch, false);
		}

		@Override
		public void setStage(int stage) {
			super.setStage(stage);
			setDataStatement(updateStage, stage, false);
		}

		@Override
		public <T> T setAdditionalData(String key, T value) {
			T additionalData = super.setAdditionalData(key, value);
			setDataStatement(updateDatas, super.additionalDatas.isEmpty() ? null : CustomizedObjectTypeAdapter.serializeNullable(super.additionalDatas), true);
			return additionalData;
		}

		@Override
		public void addQuestFlow(StageController finished) {
			super.addQuestFlow(finished);
			setDataStatement(updateFlow, getQuestFlow(), true);
		}

		@Override
		public void resetQuestFlow() {
			super.resetQuestFlow();
			setDataStatement(updateFlow, null, true);
		}

		private void setDataStatement(String dataStatement, Object data, boolean allowNull) {
			if (disabled) return;
			try {
				datasLock.lock();
				if (disabled) {
					// in case disabled while acquiring lock
				}else if (cachedDatas.containsKey(dataStatement)) {
					cachedDatas.get(dataStatement).setValue(data);
				}else {
					Runnable run = () -> {
						if (disabled) return;
						Entry<ScheduledFuture<?> , Object> entry = null;
						datasLock.lock();
						try {
							if (!disabled) { // in case disabled while acquiring lock
								entry = cachedDatas.remove(dataStatement);
							}
						}finally {
							datasLock.unlock();
						}
						if (entry != null) {
							try {
								if (dbLock.tryLock(DATA_QUERY_TIMEOUT, TimeUnit.SECONDS)) {
									try (Connection connection = db.getConnection()) {
										if (dbId == -1) createDataRow(connection);
										try (PreparedStatement statement = connection.prepareStatement(dataStatement)) {
											statement.setObject(1, entry.getValue());
											statement.setInt(2, dbId);
											statement.setQueryTimeout(DATA_QUERY_TIMEOUT);
											statement.executeUpdate();
											if (entry.getValue() == null && !allowNull) {
												QuestsPlugin.getPlugin().getLoggerExpanded().warning("Setting an illegal NULL value in statement \"" + dataStatement + "\" for account " + acc.index + " and quest " + questID);
											}
										}
									} catch (Exception ex) {
										QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while updating a player's quest datas.", ex);
									}finally {
										dbLock.unlock();
									}
								}else {
									QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot acquire database lock for quest " + questID + ", player " + acc.getNameAndID());
								}
							}catch (InterruptedException ex) {
								QuestsPlugin.getPlugin().getLoggerExpanded().severe("Interrupted database locking.", ex);
								Thread.currentThread().interrupt();
							}
						}
					};
					ScheduledFuture<?> runnable = Executors.newSingleThreadScheduledExecutor().
							schedule(run, DATA_FLUSHING_TIME * 50, TimeUnit.MILLISECONDS);
					cachedDatas.put(dataStatement, new AbstractMap.SimpleEntry<>(runnable, data));

				}
			}finally {
				datasLock.unlock();
			}
		}

		protected void flushAll(boolean stop) {
			try {
				if (datasLock.tryLock(DATA_QUERY_TIMEOUT * 2L, TimeUnit.SECONDS)) {
					cachedDatas.values().stream().map(Entry::getKey).collect(Collectors.toList()) // to prevent ConcurrentModificationException
							.forEach(run -> {
                                try {
									run.cancel(true);
                                    run.get();
                                } catch (InterruptedException | ExecutionException e) {
									QuestsPlugin.getPlugin().getLogger().severe(e.getMessage());
                                }
                            });
					if (!cachedDatas.isEmpty()) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Still waiting values in quest data " + questID + " for account " + acc.index + " despite flushing all.");
					if (stop) disabled = true;
					datasLock.unlock();
				}else {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot acquire database lock to save all datas of quest " + questID + ", player " + acc.getNameAndID());
				}
			}catch (InterruptedException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Interrupted database locking.", ex);
				Thread.currentThread().interrupt();
			}
		}

		protected void stop() {
			disabled = true;
			datasLock.lock();
			cachedDatas.values()
				.stream()
				.map(Entry::getKey)
				.forEach(scheduledFuture -> scheduledFuture.cancel(true));
			cachedDatas.clear();
			datasLock.unlock();
		}

		private void createDataRow(Connection connection) throws SQLException {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Inserting DB row of quest " + questID + " for account " + acc.index);
			try (PreparedStatement insertStatement = connection.prepareStatement(insertQuestData, new String[] {"id"})) {
				insertStatement.setInt(1, acc.index);
				insertStatement.setInt(2, questID);
				insertStatement.setQueryTimeout(DATA_QUERY_TIMEOUT);
				int affectedLines = insertStatement.executeUpdate();
				if (affectedLines != 1)
					throw new DataException("No row inserted");
				ResultSet generatedKeys = insertStatement.getGeneratedKeys();
				if (!generatedKeys.next())
					throw new DataException("Generated keys ResultSet is empty");
				dbId = generatedKeys.getInt(1);
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Created row " + dbId + " for quest " + questID + ", account " + acc.index);
			}
		}

	}

	public class PlayerPoolDatasDB extends PlayerPoolDatasImplementation {

		public PlayerPoolDatasDB(PlayerAccountImplementation acc, int poolID) {
			super(acc, poolID);
		}

		public PlayerPoolDatasDB(PlayerAccountImplementation acc, int poolID, long lastGive, Set<Integer> completedQuests) {
			super(acc, poolID, lastGive, completedQuests);
		}

		@Override
		public void setLastGive(long lastGive) {
			super.setLastGive(lastGive);
			updateData(updatePoolLastGive, lastGive);
		}

		@Override
		public void updatedCompletedQuests() {
			updateData(updatePoolCompletedQuests, getCompletedQuestsString(getCompletedQuests()));
		}

		private void updateData(String dataStatement, Object data) {
			try (Connection connection = db.getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement(getPoolAccountData)) {
					statement.setInt(1, acc.index);
					statement.setInt(2, poolID);
					if (!statement.executeQuery().next()) { // if result set empty => need to insert data then update
						try (PreparedStatement insertStatement = connection.prepareStatement(insertPoolData)) {
							insertStatement.setInt(1, acc.index);
							insertStatement.setInt(2, poolID);
							insertStatement.executeUpdate();
						}
					}
				}
				try (PreparedStatement statement = connection.prepareStatement(dataStatement)) {
					statement.setObject(1, data);
					statement.setInt(2, acc.index);
					statement.setInt(3, poolID);
					statement.executeUpdate();
				}
			}catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while updating a player's pool datas.", ex);
			}
		}

	}

	public class PlayerAccountDB extends PlayerAccountImplementation {

		public PlayerAccountDB(AbstractAccount account, int index) {
			super(account, index);
		}

		@Override
		public <T> void setData(SavableData<T> data, T value) {
			super.setData(data, value);

			SQLDataSaver<T> dataSaver = (SQLDataSaver<T>) accountDatas.get(data);
			try (Connection connection = db.getConnection();
					PreparedStatement statement = connection.prepareStatement(dataSaver.getUpdateStatement())) {
				dataSaver.setInStatement(statement, 1, value);
				statement.setInt(2, index);
				statement.executeUpdate();
			}catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while saving account data " + data.getId() + " to database", ex);
			}
		}

		@Override
		public void resetDatas() {
			super.resetDatas();

			if (resetAccountDatas != null) {
				try (Connection connection = db.getConnection();
						PreparedStatement statement = connection.prepareStatement(resetAccountDatas)) {
					statement.setInt(1, index);
					statement.executeUpdate();
				}catch (SQLException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while resetting account " + index + " datas from database", ex);
				}
			}
		}

	}

}
