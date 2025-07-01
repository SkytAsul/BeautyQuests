package fr.skytasul.quests.questers.data.sql;

import static fr.skytasul.quests.questers.data.sql.SqlQuesterData.fillInOptionalInt;
import static fr.skytasul.quests.questers.data.sql.SqlQuesterData.fillInOptionalLong;
import static fr.skytasul.quests.questers.data.sql.SqlQuesterData.fillInSerializable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import fr.skytasul.quests.utils.Database;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SqlDataManager implements QuesterDataManager {

	protected static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.SqlDataManager");
	private final SqlHandler sqlHandler;

	private final ExecutorService dataExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		int i = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "BeautyQuests-sql-data-" + i);
		}
	});

	public SqlDataManager(@NotNull Database db) {
		this.sqlHandler = new SqlHandler(db);
	}

	public @NotNull SqlHandler getSqlHandler() {
		return sqlHandler;
	}

	/**
	 * Used internally to easily create a database connection without having to write everything
	 *
	 * @return a database connection
	 * @throws SQLException
	 */
	protected @NotNull Connection getDbConnection() throws SQLException {
		return sqlHandler.getDatabase().getConnection();
	}

	protected @NotNull Executor getDataExecutor() {
		return dataExecutor;
	}

	@Override
	public void load() throws DataLoadingException {
		try {
			sqlHandler.createTables(QuestsAPI.getAPI().getQuesterManager());
		} catch (SQLException ex) {
			throw new DataLoadingException("Failed to create database tables", ex);
		}
		sqlHandler.initializeStatements();
	}

	@Override
	public @NotNull CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			try (var connection = getDbConnection();
					var getStatement = connection.prepareStatement(sqlHandler.getQuesterData)) {
				getStatement.setString(1, request.providerKey().asString());
				getStatement.setString(2, request.identifier());

				var result = getStatement.executeQuery();
				if (result.next()) {
					// means there is data for this provider/identifier pair
					var data = new SqlQuesterData(this, request.providerKey(), request.identifier());
					data.load(result);

					return new QuesterFetchResult(QuesterFetchResult.Type.SUCCESS_LOADED, data);
				} else {
					// there is no data for this provider/identifier pair
					if (!request.createIfMissing())
						return new QuesterFetchResult(QuesterFetchResult.Type.FAILED_NOT_FOUND, null);

					try (var insertStatement = connection.prepareStatement(sqlHandler.insertAccount)) {
						insertStatement.setString(1, request.providerKey().asString());
						insertStatement.setString(2, request.identifier());
						insertStatement.executeUpdate();

						return new QuesterFetchResult(QuesterFetchResult.Type.SUCCESS_CREATED,
								new SqlQuesterData(this, request.providerKey(), request.identifier()));
					}
				}
			} catch (SQLException | DataLoadingException ex) {
				throw new CompletionException(ex);
			}
		}, dataExecutor);
	}

	@Override
	public CompletableFuture<Integer> resetQuestData(int questId) {
		return CompletableFuture.supplyAsync(() -> {
			try (var connection = getDbConnection();
					var statement = connection.prepareStatement(sqlHandler.removeExistingQuestDatas)) {
				statement.setInt(1, questId);
				int amount = statement.executeUpdate();
				LOGGER.debug("Removed {} in-database quest datas for quest {}.", amount, questId);
				return amount;
			} catch (SQLException ex) {
				throw new CompletionException(ex);
			}
		}, dataExecutor);
	}

	@Override
	public CompletableFuture<Integer> resetPoolData(int poolId) {
		return CompletableFuture.supplyAsync(() -> {
			try (var connection = getDbConnection();
					var statement = connection.prepareStatement(sqlHandler.removeExistingPoolDatas)) {
				statement.setInt(1, poolId);
				int amount = statement.executeUpdate();
				LOGGER.debug("Removed {} in-database data for pool {}.", amount, poolId);
				return amount;
			} catch (SQLException ex) {
				throw new CompletionException(ex);
			}
		}, dataExecutor);
	}

	private void importQuester(@NotNull QuesterData data, @NotNull Connection connection) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO %s (provider, identifier) VALUES (?, ?)".formatted(sqlHandler.QUESTERS_TABLE));
		int i = 1;
		statement.setString(i++, data.provider().asString());
		statement.setString(i++, data.identifier());
		// TODO additional data
		statement.executeUpdate();

		statement = connection.prepareStatement(
				"INSERT INTO %s (quester_provider, quester_identifier, quest_id, finished, timer, current_branch, current_stage, starting_time, stage_data, additional_datas, state, quest_flow) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
						.formatted(sqlHandler.QUESTS_DATAS_TABLE));
		for (var quest : data.getAllQuestsData()) {
			i = 1;
			statement.setString(i++, data.provider().asString());
			statement.setString(i++, data.identifier());
			statement.setInt(i++, quest.getQuestId());
			statement.setInt(i++, quest.getTimesFinished());
			fillInOptionalLong(statement, i++, quest.getTimer());
			fillInOptionalInt(statement, i++, quest.getBranch());
			fillInOptionalInt(statement, i++, quest.getStage());
			fillInOptionalLong(statement, i++, quest.getStartingTime());
			fillInSerializable(statement, i++, quest.getAllStagesData());
			fillInSerializable(statement, i++, quest.getAllAdditionalData());
			statement.setString(i++, quest.getState().name());
			statement.setString(i++, quest.getQuestFlowStages().stream().map(StageController::getFlowId)
					.collect(Collectors.joining(";")));
			statement.addBatch();
		}
		statement.executeBatch();

		statement = connection.prepareStatement(
				"INSERT INTO %s (quester_provider, quester_identifier, pool_id, last_give, completed_quests) VALUES (?, ?, ?, ?, ?)"
						.formatted(sqlHandler.POOLS_DATAS_TABLE));
		for (var pool : data.getAllPoolsData()) {
			i = 1;
			statement.setString(i++, data.provider().asString());
			statement.setString(i++, data.identifier());
			statement.setInt(i++, pool.getPoolId());
			statement.setLong(i++, pool.getLastGive());
			statement.setString(i++,
					pool.getCompletedQuests().stream().map(String::valueOf).collect(Collectors.joining(";")));
			statement.addBatch();
		}
		statement.executeBatch();
	}

	@Override
	public @NotNull CompletableFuture<ImportResult> importAll(@NotNull Iterator<? extends QuesterData> iterator) {
		return CompletableFuture.supplyAsync(() -> {
			int successes = 0, failures = 0;
			try (var connection = getDbConnection()) {
				while (iterator.hasNext()) {
					var questerData = iterator.next();
					try {
						importQuester(questerData, connection);
						successes++;
					} catch (Exception ex) {
						LOGGER.severe("Failed to import quester %s %s", questerData.provider(), questerData.identifier());
						failures++;
					}
				}
			} catch (SQLException ex) {
				throw new CompletionException(ex);
			}
			return new ImportResult(successes, failures);
		}, dataExecutor);
	}

	@Override
	public void save() throws DataSavingException {
		// data is saved in real-time
	}

	@Override
	public void unload() {
		dataExecutor.shutdown();
		try {
			dataExecutor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

}
