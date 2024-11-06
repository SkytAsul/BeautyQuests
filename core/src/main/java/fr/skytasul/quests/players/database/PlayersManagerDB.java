package fr.skytasul.quests.players.database;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.players.AbstractPlayersManager;
import fr.skytasul.quests.players.DataException;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.yaml.PlayersManagerYAML;
import fr.skytasul.quests.utils.Database;
import org.jetbrains.annotations.NotNull;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayersManagerDB extends AbstractPlayersManager<PlayerAccountDB> {

	private final PlayerDatabaseHandler databaseHandler;

	final Map<SavableData<?>, SQLDataSaver<?>> accountDatas = new HashMap<>();

	public PlayersManagerDB(Database db) {
		this.databaseHandler = new PlayerDatabaseHandler(db);
	}

	protected @NotNull PlayerDatabaseHandler getDatabaseHandler() {
		return databaseHandler;
	}

	protected @NotNull Connection getDbConnection() throws SQLException {
		return databaseHandler.getDatabase().getConnection();
	}

	@Override
	public void addAccountData(SavableData<?> data) {
		super.addAccountData(data);
		accountDatas.put(data,
				new SQLDataSaver<>(data,
						"UPDATE " + databaseHandler.ACCOUNTS_TABLE + " SET " + data.getColumnName() + " = ? WHERE id = ?"));
		databaseHandler.getAccountDatas = accountDatas.keySet()
				.stream()
				.map(SavableData::getColumnName)
				.collect(Collectors.joining(", ", "SELECT ", " FROM " + databaseHandler.ACCOUNTS_TABLE + " WHERE id = ?"));
		databaseHandler.resetAccountDatas = accountDatas.values()
				.stream()
				.map(x -> x.getWrappedData().getColumnName() + " = " + x.getDefaultValueString())
				.collect(Collectors.joining(", ", "UPDATE " + databaseHandler.ACCOUNTS_TABLE + " SET ", " WHERE id = ?"));
	}

	@Override
	public void load(AccountFetchRequest<PlayerAccountDB> request) {
		try (Connection connection = getDbConnection()) {
			AbstractAccount abs = newAbstractAccount(request.getOfflinePlayer())
					.orElseThrow(() -> new IllegalArgumentException(
							"Cannot find account for player " + request.getOfflinePlayer().getName()));

			try (PreparedStatement statement = connection.prepareStatement(databaseHandler.getAccountId)) {
				statement.setString(1, abs.getIdentifier());
				ResultSet result = statement.executeQuery();
				if (result.next()) {
					// means BQ has data for the account with the identifier
					if (abs.isCurrent()) {
						var account = new PlayerAccountDB(this, abs, result.getInt("id"));
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
						account.retrievePlayerDatas();
						request.loaded(account, "database");
						return;
					}
				}
			}

			if (request.mustCreateMissing()) {
				try (PreparedStatement statement =
						connection.prepareStatement(databaseHandler.insertAccount, Statement.RETURN_GENERATED_KEYS)) {
					statement.setString(1, abs.getIdentifier());
					statement.setString(2, request.getOnlinePlayer().getUniqueId().toString());
					statement.executeUpdate();
					ResultSet result = statement.getGeneratedKeys();
					if (!result.next())
						throw new SQLException("The plugin has not been able to create a player account.");
					int index = result.getInt(1); // some drivers don't return a ResultSet with correct column names
					request.created(new PlayerAccountDB(this, abs, index));
				}
			} else {
				request.notLoaded();
			}
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while loading account of " + request.getDebugPlayerName(), ex);
		}
	}

	@Override
	protected CompletableFuture<Void> removeAccount(PlayerAccountDB acc) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = getDbConnection();
					PreparedStatement statement = connection.prepareStatement(databaseHandler.deleteAccount)) {
				statement.setInt(1, acc.index);
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing account from database.", ex);
			}
		});
	}

	@Override
	public CompletableFuture<Integer> removeQuestDatas(Quest quest) {
		return CompletableFuture.supplyAsync(() -> {
			try (Connection connection = getDbConnection();
					PreparedStatement statement = connection.prepareStatement(databaseHandler.removeExistingQuestDatas)) {
				for (PlayerAccountDB acc : cachedAccounts.values()) {
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

	@Override
	public CompletableFuture<Integer> removePoolDatas(QuestPool pool) {
		return CompletableFuture.supplyAsync(() -> {
			try (Connection connection = getDbConnection();
					PreparedStatement statement = connection.prepareStatement(databaseHandler.removeExistingPoolDatas)) {
				for (var acc : cachedAccounts.values()) {
					acc.removePoolDatasSilently(pool.getId());
				}
				statement.setInt(1, pool.getId());
				int amount = statement.executeUpdate();
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.debug("Removed " + amount + " in-database pool datas for pool " + pool.getId());
				return amount;
			} catch (SQLException ex) {
				throw new DataException("Failed to remove quest datas from database.", ex);
			}
		});
	}

	@Override
	public void load() throws DataException {
		super.load();
		try {
			databaseHandler.createTables(this);
			databaseHandler.initializeStatements();
		}catch (SQLException e) {
			throw new DataException("Failed to initialize database", e);
		}
	}

	@Override
	public void save() {
		cachedAccounts.values().forEach(x -> x.save(false));
	}

	public static synchronized String migrate(Database db, PlayersManagerYAML yaml) throws SQLException {
		try (Connection connection = db.getConnection()) {
			var databaseHandler = new PlayerDatabaseHandler(db);

			ResultSet result = connection.getMetaData().getTables(null, null, "%", null);
			while (result.next()) {
				String tableName = result.getString(3);
				if (tableName.equals(databaseHandler.ACCOUNTS_TABLE)
						|| tableName.equals(databaseHandler.QUESTS_DATAS_TABLE)
						|| tableName.equals(databaseHandler.POOLS_DATAS_TABLE)) {
					result.close();
					return "§cTable \"" + tableName + "\" already exists. Please drop it before migration.";
				}
			}
			result.close();

			databaseHandler.createTables(new PlayersManagerDB(db));

			PreparedStatement insertAccount =
					connection.prepareStatement(
							"INSERT INTO " + databaseHandler.ACCOUNTS_TABLE
									+ " (id, identifier, player_uuid) VALUES (?, ?, ?)");
			PreparedStatement insertQuestData =
					connection.prepareStatement("INSERT INTO " + databaseHandler.QUESTS_DATAS_TABLE
							+ " (account_id, quest_id, finished, timer, current_branch, current_stage, additional_datas, quest_flow) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			PreparedStatement insertPoolData =
					connection.prepareStatement("INSERT INTO " + databaseHandler.POOLS_DATAS_TABLE
							+ " (account_id, pool_id, last_give, completed_quests) VALUES (?, ?, ?, ?)");

			int amount = 0, failed = 0;
			var yamlAccounts = yaml.loadAllAccounts();
			for (PlayerAccountImplementation acc : yamlAccounts) {
				try {
					insertAccount.setInt(1, acc.index);
					insertAccount.setString(2, acc.abstractAcc.getIdentifier());
					insertAccount.setString(3, acc.getOfflinePlayer().getUniqueId().toString());
					insertAccount.executeUpdate();

					for (var questDatas : acc.getQuestsDatas()) {
						insertQuestData.setInt(1, acc.index);
						insertQuestData.setInt(2, questDatas.getQuestID());
						insertQuestData.setInt(3, questDatas.getTimesFinished());
						insertQuestData.setLong(4, questDatas.getTimer());
						insertQuestData.setInt(5, questDatas.getBranch());
						insertQuestData.setInt(6, questDatas.getStage());
						insertQuestData.setString(7, questDatas.getRawAdditionalDatas().isEmpty() ? null
								: CustomizedObjectTypeAdapter.serializeNullable(questDatas.getRawAdditionalDatas()));
						insertQuestData.setString(8, questDatas.getQuestFlow());
						insertQuestData.executeUpdate();
					}

					for (var poolDatas : acc.getPoolDatas()) {
						insertPoolData.setInt(1, acc.index);
						insertPoolData.setInt(2, poolDatas.getPoolID());
						insertPoolData.setLong(3, poolDatas.getLastGive());
						insertPoolData.setString(4, getCompletedQuestsString(poolDatas.getCompletedQuests()));
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

	protected static String getCompletedQuestsString(Set<Integer> completedQuests) {
		return completedQuests.isEmpty() ? null : completedQuests.stream().map(x -> Integer.toString(x)).collect(Collectors.joining(";"));
	}

}
