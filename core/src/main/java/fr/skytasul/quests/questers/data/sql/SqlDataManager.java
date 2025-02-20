package fr.skytasul.quests.questers.data.sql;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import fr.skytasul.quests.utils.Database;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;

public class SqlDataManager implements QuesterDataManager {

	private final SqlHandler sqlHandler;

	private final ExecutorService dataExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
		int i = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "BeautyQuests-sql-data-" + i);
		}
	});

	public SqlDataManager(@NotNull Database db) {
		this.sqlHandler = new SqlHandler(db);
	}

	protected @NotNull SqlHandler getSqlHandler() {
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
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Removed {} in-database quest datas for quest {}.",
						amount, questId);
				return amount;
			} catch (SQLException ex) {
				throw new CompletionException(ex);
			}
		}, dataExecutor);
	}

	@Override
	public CompletableFuture<Integer> resetPoolData(int poolId) {
		// TODO
		return null;
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
