package fr.skytasul.quests.players.database;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerDatabaseHandler {

	public final String ACCOUNTS_TABLE;
	public final String QUESTS_DATAS_TABLE;
	public final String POOLS_DATAS_TABLE;

	String getAccountDatas;
	String resetAccountDatas;

	/* Accounts statements */
	String getAccountId;
	String insertAccount;
	String deleteAccount;

	/* Quest datas statements */
	String insertQuestData;
	String removeQuestData;
	String getQuestsData;

	String removeExistingQuestDatas;
	String removeExistingPoolDatas;

	String updateFinished;
	String updateTimer;
	String updateBranch;
	String updateStage;
	String updateDatas;
	String updateFlow;

	/* Pool datas statements */
	String insertPoolData;
	String removePoolData;
	String getPoolData;
	String getPoolAccountData;

	String updatePoolLastGive;
	String updatePoolCompletedQuests;

	private final Database db;

	public PlayerDatabaseHandler(@NotNull Database db) {
		this.db = db;

		ACCOUNTS_TABLE = db.getConfig().getString("tables.playerAccounts");
		QUESTS_DATAS_TABLE = db.getConfig().getString("tables.playerQuests");
		POOLS_DATAS_TABLE = db.getConfig().getString("tables.playerPools");
	}

	public Database getDatabase() {
		return db;
	}

	public void createTables(@NotNull PlayersManagerDB playersManager) throws SQLException {
		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE + " ("
					+ " id " + db.getType().getSerialType() + " ,"
					+ " identifier TEXT NOT NULL ,"
					+ " player_uuid CHAR(36) NOT NULL ,"
					+ playersManager.accountDatas.values().stream().map(data -> " " + data.getColumnDefinition() + " ,")
							.collect(Collectors.joining())
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
					// tests for stage_0_datas: it's in the case the server crashed/stopped during the migration
					// process.
					if (!columns.contains("additional_datas")) {
						statement.execute("ALTER TABLE " + QUESTS_DATAS_TABLE
								+ " ADD COLUMN additional_datas " + db.getType().getLongTextType()
								+ " DEFAULT NULL AFTER current_stage");
						QuestsPlugin.getPlugin().getLoggerExpanded()
								.info("Updated table " + QUESTS_DATAS_TABLE + " with additional_datas column.");
					}

					QuestUtils.runAsync(this::migrateOldQuestDatas);
				}
			});

			upgradeTable(connection, ACCOUNTS_TABLE, columns -> {
				for (SQLDataSaver<?> data : playersManager.accountDatas.values()) {
					if (!columns.contains(data.getWrappedData().getColumnName().toLowerCase())) {
						statement.execute("ALTER TABLE " + ACCOUNTS_TABLE
								+ " ADD COLUMN " + data.getColumnDefinition());
						QuestsPlugin.getPlugin().getLoggerExpanded().info("Updated database by adding the missing "
								+ data.getWrappedData().getColumnName() + " column in the player accounts table.");
					}
				}
			});
		}
	}

	private void upgradeTable(Connection connection, String tableName,
			ThrowingConsumer<List<String>, SQLException> columnsConsumer) throws SQLException {
		List<String> columns = new ArrayList<>(14);
		try (ResultSet set = connection.getMetaData().getColumns(db.getDatabase(), null, tableName, null)) {
			while (set.next()) {
				columns.add(set.getString("COLUMN_NAME").toLowerCase());
			}
		}
		if (columns.isEmpty()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot check integrity of SQL table " + tableName);
		} else {
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
			if (deletedDuplicates > 0)
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.info("Deleted " + deletedDuplicates + " duplicated rows in the " + QUESTS_DATAS_TABLE + " table.");

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
					if (stageDatas != null && !"{}".equals(stageDatas))
						datas.put("stage" + i, CustomizedObjectTypeAdapter.deserializeNullable(stageDatas, Map.class));
				}

				if (datas.isEmpty())
					continue;
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
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.info("Updated database by deleting old stage_[0::4]_datas columns.");
			QuestsPlugin.getPlugin().getLoggerExpanded().info("---- CAUTION ----\n"
					+ "The data migration succeeded. Players can now safely connect.");
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("---- CAUTION ----\n"
					+ "The plugin failed to migrate old quest datas in database.", ex);
		}
	}

	protected void initializeStatements() {
		getAccountId = "SELECT id FROM " + ACCOUNTS_TABLE + " WHERE identifier = ?";
		insertAccount = "INSERT INTO " + ACCOUNTS_TABLE + " (identifier, player_uuid) VALUES (?, ?)";
		deleteAccount = "DELETE FROM " + ACCOUNTS_TABLE + " WHERE id = ?";

		insertQuestData = "INSERT INTO " + QUESTS_DATAS_TABLE + " (account_id, quest_id) VALUES (?, ?)";
		removeQuestData = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE account_id = ? AND quest_id = ?";
		getQuestsData = "SELECT * FROM " + QUESTS_DATAS_TABLE + " WHERE account_id = ?";

		removeExistingQuestDatas = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE quest_id = ?";
		removeExistingPoolDatas = "DELETE FROM " + POOLS_DATAS_TABLE + " WHERE pool_id = ?";

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
	}

	private String prepareDatasStatement(String column) {
		return "UPDATE " + QUESTS_DATAS_TABLE + " SET " + column + " = ? WHERE id = ?";
	}

}
