package fr.skytasul.quests.questers.data.sql;

import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.players.PlayerManagerImplementation;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SqlHandler {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.SqlDataHandler");

	public final String QUESTERS_TABLE;
	public final String QUESTS_DATAS_TABLE;
	public final String POOLS_DATAS_TABLE;

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

	/* Pool datas statements */
	String insertPoolData;
	String removePoolData;
	String getPoolsData;
	String removeExistingPoolDatas;

	private final Database db;

	public SqlHandler(@NotNull Database db) {
		this.db = db;

		QUESTERS_TABLE = db.getConfig().tables().get("questers");
		QUESTS_DATAS_TABLE = db.getConfig().tables().get("questers quests");
		POOLS_DATAS_TABLE = db.getConfig().tables().get("questers pools");
	}

	public Database getDatabase() {
		return db;
	}

	public void createTables(@NotNull QuesterManager questerManager) throws SQLException {
		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + QUESTERS_TABLE + " ("
					+ " provider VARCHAR(255) NOT NULL ,"
					+ " identifier VARCHAR(255) NOT NULL , "
					+ questerManager.getSavableData().stream()
							.map(t -> SQLDataSaver.getColumnDefinition(t) + ", ")
							.collect(Collectors.joining(" "))
					+ " PRIMARY KEY (provider, identifier)"
					+ " )");
			statement.execute("""
					CREATE TABLE IF NOT EXISTS %s (
						quester_provider VARCHAR(255) NOT NULL,
						quester_identifier VARCHAR(255) NOT NULL,
						quest_id INT NOT NULL,
						finished INT NOT NULL DEFAULT 0,
						timer BIGINT DEFAULT NULL,
						current_branch SMALLINT DEFAULT NULL,
						current_stage SMALLINT DEFAULT NULL,
						starting_time BIGINT DEFAULT NULL,
						stage_data %2$s DEFAULT NULL,
						additional_datas %2$s DEFAULT NULL,
						state VARCHAR(60) DEFAULT 'NOT_STARTED',
						quest_flow VARCHAR(8000) DEFAULT NULL,
						PRIMARY KEY (quester_provider, quester_identifier, quest_id)
					)
					""".formatted(QUESTS_DATAS_TABLE, db.getType().getLongTextType()));
			statement.execute("""
					CREATE TABLE IF NOT EXISTS %s (
						quester_provider VARCHAR(255) NOT NULL,
						quester_identifier VARCHAR(255) NOT NULL,
						pool_id INT NOT NULL,
						last_give BIGINT DEFAULT NULL,
						completed_quests VARCHAR(1000) DEFAULT NULL,
						PRIMARY KEY (quester_provider, quester_identifier, pool_id)
					)
					""".formatted(POOLS_DATAS_TABLE));

			var questersHasId = new AtomicBoolean(false);

			upgradeTable(connection, QUESTERS_TABLE, columns -> {
				for (SavableData<?> data : questerManager.getSavableData()) {
					if (!columns.contains(data.getColumnName().toLowerCase())) {
						statement.execute("ALTER TABLE %s ADD COLUMN %s".formatted(QUESTERS_TABLE,
								SQLDataSaver.getColumnDefinition(data)));
						LOGGER.info(
								"Updated database by adding the missing {} column in the player accounts table.",
								data.getColumnName());
					}
				}

				if (!columns.contains("provider")) {
					// 2.0
					statement.execute("""
							ALTER TABLE %1$s
								DROP COLUMN player_uuid,

								-- delete the auto_increment
								MODIFY COLUMN id int(11) NOT NULL,
								DROP PRIMARY KEY,

								ADD COLUMN provider VARCHAR(255) NOT NULL,
								MODIFY COLUMN identifier VARCHAR(255) NOT NULL,
								ADD PRIMARY KEY (provider, identifier)
							""".formatted(QUESTERS_TABLE));
					statement.execute("UPDATE %s SET provider = '%s'".formatted(QUESTERS_TABLE,
							PlayerManagerImplementation.KEY.asString()));

					LOGGER
							.info("Updated database by changing layout of the questers table.");
				}

				if (columns.contains("id"))
					questersHasId.set(true);
			});

			upgradeTable(connection, QUESTS_DATAS_TABLE, columns -> {
				if (columns.contains("account_id")) {
					// 2.0
					statement.execute("""
							ALTER TABLE %1$s
								ADD COLUMN starting_time BIGINT DEFAULT NULL,
								ADD COLUMN stage_data %2$s DEFAULT NULL,
								ADD COLUMN state VARCHAR(60) DEFAULT 'NOT_STARTED',

								ADD COLUMN quester_provider VARCHAR(225) NOT NULL,
								ADD COLUMN quester_identifier VARCHAR(255) NOT NULL
							""".formatted(QUESTS_DATAS_TABLE, db.getType().getLongTextType()));
					statement.execute("UPDATE %s SET state = NULL".formatted(QUESTS_DATAS_TABLE));
					statement.execute("UPDATE %s SET quester_identifier = '_migration'".formatted(QUESTS_DATAS_TABLE));
					statement.execute("UPDATE %s SET quester_provider = '%s'".formatted(QUESTS_DATAS_TABLE,
							PlayerManagerImplementation.KEY.asString()));
					statement.execute("""
							UPDATE %1$s AS quests
								INNER JOIN %2$s AS questers ON quests.account_id = questers.id
							SET quests.quester_identifier = questers.identifier
							""".formatted(QUESTS_DATAS_TABLE, QUESTERS_TABLE));
					int missingQuesters = statement.executeUpdate(
							"DELETE FROM %s WHERE quester_identifier = '_migration'".formatted(QUESTS_DATAS_TABLE));
					if (missingQuesters > 0)
						LOGGER.warning("%d quest data have no associated questers. Deleting them.", missingQuesters);
					statement.execute("""
							ALTER TABLE %1$s
								MODIFY COLUMN id int(11) NOT NULL,
								DROP PRIMARY KEY,
								ADD PRIMARY KEY (quester_provider, quester_identifier, quest_id)
							""".formatted(QUESTS_DATAS_TABLE));
					// we cannot modify a column and drop it in the same alter statement
					statement.execute("""
							ALTER TABLE %1$s
								DROP COLUMN id,
								DROP COLUMN account_id
							""".formatted(QUESTS_DATAS_TABLE));
					LOGGER.info("Updated database by changing layout of the quests data table.");
				}
			});

			upgradeTable(connection, POOLS_DATAS_TABLE, columns -> {
				if (columns.contains("account_id")) {
					// 2.0
					statement.execute("""
							ALTER TABLE %1$s
								ADD COLUMN quester_provider VARCHAR(225) NOT NULL,
								ADD COLUMN quester_identifier VARCHAR(255) NOT NULL
							""".formatted(POOLS_DATAS_TABLE));
					statement.execute("UPDATE %s SET quester_identifier = '_migration', quester_provider = '%s'"
							.formatted(POOLS_DATAS_TABLE, PlayerManagerImplementation.KEY.asString()));
					statement.execute("""
							UPDATE %1$s AS pools
								INNER JOIN %2$s AS questers ON pools.account_id = questers.id
							SET pools.quester_identifier = questers.identifier
							""".formatted(POOLS_DATAS_TABLE, QUESTERS_TABLE));
					int missingQuesters = statement.executeUpdate(
							"DELETE FROM %s WHERE quester_identifier = '_migration'".formatted(POOLS_DATAS_TABLE));
					if (missingQuesters > 0)
						LOGGER.warning("%d pool data have no associated questers. Deleting them.", missingQuesters);
					statement.execute("""
							ALTER TABLE %1$s
								MODIFY COLUMN id int(11) NOT NULL,
								DROP PRIMARY KEY,
								ADD PRIMARY KEY (quester_provider, quester_identifier, pool_id)
							""".formatted(POOLS_DATAS_TABLE));
					// we cannot modify a column and drop it in the same alter statement
					statement.execute("""
							ALTER TABLE %1$s
								DROP COLUMN id,
								DROP COLUMN account_id
							""".formatted(POOLS_DATAS_TABLE));
					LOGGER
							.info("Updated database by changing layout of the pools data table.");
				}
			});

			if (questersHasId.get()) {
				statement.execute("ALTER TABLE %s DROP COLUMN id".formatted(QUESTERS_TABLE));
				LOGGER.info("Dropped the legacy id column of the questers table.");
			}
		}
	}

	private void upgradeTable(Connection connection, String tableName,
			ThrowingConsumer<List<String>, SQLException> columnsConsumer) throws SQLException {
		List<String> columns = new ArrayList<>(14);
		try (ResultSet set = connection.getMetaData().getColumns(db.getConfig().databaseName(), null, tableName, null)) {
			while (set.next()) {
				columns.add(set.getString("COLUMN_NAME").toLowerCase());
			}
		}
		if (columns.isEmpty()) {
			LOGGER.severe("Cannot check integrity of SQL table " + tableName);
		} else {
			columnsConsumer.accept(columns);
		}
	}

	protected void initializeStatements() {
		getQuesterData = "SELECT * FROM " + QUESTERS_TABLE + " WHERE provider = ? AND identifier = ?";
		setQuesterAdditionalData = "UPDATE " + QUESTERS_TABLE + " SET %s = ? WHERE provider = ? AND identifier = ?";
		insertAccount = "INSERT INTO " + QUESTERS_TABLE + " (provider, identifier) VALUES (?, ?)";
		deleteAccount = "DELETE FROM " + QUESTERS_TABLE + " WHERE provider = ? AND identifier = ?";

		insertQuestData =
				"INSERT INTO " + QUESTS_DATAS_TABLE + " (quester_provider, quester_identifier, quest_id) VALUES (?, ?, ?)";
		removeQuestData = "DELETE FROM " + QUESTS_DATAS_TABLE
				+ " WHERE quester_provider = ? AND quester_identifier = ? AND quest_id = ?";
		getQuestsData = "SELECT * FROM " + QUESTS_DATAS_TABLE + " WHERE quester_provider = ? AND quester_identifier = ?";
		removeExistingQuestDatas = "DELETE FROM " + QUESTS_DATAS_TABLE + " WHERE quest_id = ?";

		insertPoolData =
				"INSERT INTO " + POOLS_DATAS_TABLE + " (quester_provider, quester_identifier, pool_id) VALUES (?, ?, ?)";
		removePoolData = "DELETE FROM " + POOLS_DATAS_TABLE
				+ " WHERE quester_provider = ? AND quester_identifier = ? AND pool_id = ?";
		getPoolsData = "SELECT * FROM " + POOLS_DATAS_TABLE + " WHERE quester_provider = ? AND quester_identifier = ?";
		removeExistingPoolDatas = "DELETE FROM " + POOLS_DATAS_TABLE + " WHERE pool_id = ?";
	}

	public String getQuestDataStatement(String column) {
		return "UPDATE %s SET %s = ? WHERE quester_provider = ? AND quester_identifier = ? AND quest_id = ?"
				.formatted(QUESTS_DATAS_TABLE, column);
	}

	public String getPoolDataStatement(String column) {
		return "UPDATE %s SET %s = ? WHERE quester_provider = ? AND quester_identifier = ? AND pool_id = ?"
				.formatted(POOLS_DATAS_TABLE, column);
	}

}
