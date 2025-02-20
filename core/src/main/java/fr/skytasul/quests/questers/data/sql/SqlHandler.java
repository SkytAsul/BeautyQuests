package fr.skytasul.quests.questers.data.sql;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO rename class, rename tables, rename fields
public class SqlHandler {

	public final String ACCOUNTS_TABLE;
	public final String QUESTS_DATAS_TABLE;
	public final String POOLS_DATAS_TABLE;

	String getAccountDatas;
	String resetAccountDatas;

	/* Accounts statements */
	String getQuesterData;
	String setQuesterAdditionalData;
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

	public SqlHandler(@NotNull Database db) {
		this.db = db;

		ACCOUNTS_TABLE = db.getConfig().getString("tables.playerAccounts");
		QUESTS_DATAS_TABLE = db.getConfig().getString("tables.playerQuests");
		POOLS_DATAS_TABLE = db.getConfig().getString("tables.playerPools");
	}

	public Database getDatabase() {
		return db;
	}

	public void createTables(@NotNull QuesterManager questerManager) throws SQLException {
		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE + " ("
					+ " provider VARCHAR(255) NOT NULL ,"
					+ " identifier TEXT NOT NULL ,"
					+ questerManager.getSavableData().stream()
							.map(SQLDataSaver::getColumnDefinition)
							.collect(Collectors.joining(" , ", " ", " ,"))
					+ " PRIMARY KEY (provider, identifier)"
					+ " )");
			statement.execute("CREATE TABLE IF NOT EXISTS " + QUESTS_DATAS_TABLE + " (" +
					" quester_provider INT NOT NULL," +
					" quester_identifier INT NOT NULL," +
					" quest_id INT NOT NULL," +
					" finished INT NOT NULL DEFAULT 0," +
					" timer BIGINT DEFAULT NULL," +
					" current_branch SMALLINT DEFAULT NULL," +
					" current_stage SMALLINT DEFAULT NULL," +
					" starting_time BIGINT DEFAULT NULL," +
					" stage_data " + db.getType().getLongTextType() + " DEFAULT NULL," +
					" additional_datas " + db.getType().getLongTextType() + " DEFAULT NULL," +
					" state VARCHAR(60) DEFAULT 'NOT_STARTED'," +
					" quest_flow VARCHAR(8000) DEFAULT NULL," +
					" PRIMARY KEY (quester_provider, quester_identifier, quest_id)" +
					")");
			statement.execute("CREATE TABLE IF NOT EXISTS " + POOLS_DATAS_TABLE + " ("
					+ " id " + db.getType().getSerialType() + " ,"
					+ "account_id INT NOT NULL, "
					+ "pool_id INT NOT NULL, "
					+ "last_give BIGINT DEFAULT NULL, "
					+ "completed_quests VARCHAR(1000) DEFAULT NULL, "
					+ "PRIMARY KEY (id)"
					+ ")");

			upgradeTable(connection, ACCOUNTS_TABLE, columns -> {
				for (SavableData<?> data : questerManager.getSavableData()) {
					if (!columns.contains(data.getColumnName().toLowerCase())) {
						statement.execute("ALTER TABLE %s ADD COLUMN %s".formatted(ACCOUNTS_TABLE,
								SQLDataSaver.getColumnDefinition(data)));
						QuestsPlugin.getPlugin().getLoggerExpanded().info(
								"Updated database by adding the missing {} column in the player accounts table.",
								data.getColumnName());
					}
				}

				if (columns.contains("id")) {
					// 2.0
					statement.execute("""
							ALTER TABLE %1$s DROP COLUMN player_uuid;

							ALTER TABLE %1$s DROP PRIMARY KEY;
							ALTER TABLE %1$s DROP COLUMN id;
							ALTER TABLE %1$s ADD COLUMN provider VARCHAR(255) NOT NULL DEFAULT 'BeautyQuests:player';
							ALTER TABLE %1$s ADD PRIMARY KEY (provider, identifier);
							""".formatted(ACCOUNTS_TABLE));

					// TODO data migration with provider/identifier, even in quests data table


					QuestsPlugin.getPlugin().getLoggerExpanded()
							.info("Updated database by changing layout of the questers table.");
				}

			});

			upgradeTable(connection, QUESTS_DATAS_TABLE, columns -> {
				if (columns.contains("account_id")) {
					// 2.0
					statement.execute("""
							-- TODO manage account_id column to delete it and add quester_provider, quester_identifier

							ALTER TABLE %1$s DROP PRIMARY KEY;
							ALTER TABLE %1$s DROP COLUMN id;
							ALTER TABLE %1$s ADD PRIMARY KEY (quester_provider, quester_identifier, quest_id);

							ALTER TABLE %1$s ADD COLUMN starting_time BIGINT DEFAULT NULL;
							ALTER TABLE %1$s ADD COLUMN stage_data %2$s DEFAULT NULL;

							-- we do it in 2 parts since we want the existing columns to hold null and not "not_started"
							ALTER TABLE %1$s ADD COLUMN state VARCHAR(60) DEFAULT NULL;
							ALTER TABLE %1$s ALTER COLUMN state SET DEFAULT 'NOT_STARTED';
							""".formatted(QUESTS_DATAS_TABLE, db.getType().getLongTextType()));
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.info("Updated database by changing layout of the quests data table.");
				}
			});

			// TODO upgrade pools
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

	protected void initializeStatements() {
		getQuesterData = "SELECT * FROM " + ACCOUNTS_TABLE + " WHERE provider = ? AND identifier = ?";
		setQuesterAdditionalData = "UPDATE " + ACCOUNTS_TABLE + " SET %s = ? WHERE provider = ? AND identifier = ?";
		insertAccount = "INSERT INTO " + ACCOUNTS_TABLE + " (provider, identifier) VALUES (?, ?)";
		deleteAccount = "DELETE FROM " + ACCOUNTS_TABLE + " WHERE provider = ? AND identifier = ?";

		insertQuestData =
				"INSERT INTO " + QUESTS_DATAS_TABLE + " (quester_provider, quester_identifier, quest_id) VALUES (?, ?, ?)";
		removeQuestData = "DELETE FROM " + QUESTS_DATAS_TABLE
				+ " WHERE quester_provider = ? AND quester_identifier = ? AND quest_id = ?";
		getQuestsData = "SELECT * FROM " + QUESTS_DATAS_TABLE + " WHERE quester_provider = ? AND quester_identifier = ?";

		removeExistingQuestDatas = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE quest_id = ?";
		removeExistingPoolDatas = "DELETE FROM " + POOLS_DATAS_TABLE + " WHERE pool_id = ?";

		insertPoolData = "INSERT INTO " + POOLS_DATAS_TABLE + " (account_id, pool_id) VALUES (?, ?)";
		removePoolData = "DELETE FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ? AND pool_id = ?";
		getPoolData = "SELECT * FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ?";
		getPoolAccountData = "SELECT 1 FROM " + POOLS_DATAS_TABLE + " WHERE account_id = ? AND pool_id = ?";

		updatePoolLastGive = "UPDATE " + POOLS_DATAS_TABLE + " SET last_give = ? WHERE account_id = ? AND pool_id = ?";
		updatePoolCompletedQuests =
				"UPDATE " + POOLS_DATAS_TABLE + " SET completed_quests = ? WHERE account_id = ? AND pool_id = ?";
	}

	public String getQuestDataStatement(String column) {
		return "UPDATE %s SET %s = ? WHERE quester_provider = ? AND quester_identifier = ? AND quest_id = ?"
				.formatted(QUESTS_DATAS_TABLE, column);
	}

}
