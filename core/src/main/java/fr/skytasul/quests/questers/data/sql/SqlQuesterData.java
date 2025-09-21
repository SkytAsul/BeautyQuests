package fr.skytasul.quests.questers.data.sql;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.data.QuesterPoolData;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageIndex;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.questers.AbstractQuesterDataImplementation;
import fr.skytasul.quests.questers.AbstractQuesterPoolDataImplementation;
import fr.skytasul.quests.questers.AbstractQuesterQuestDataImplementation;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlQuesterData extends AbstractQuesterDataImplementation {

	private final @NotNull SqlDataManager dataManager;
	private final @NotNull Key provider;
	private final @NotNull String identifier;

	public SqlQuesterData(@NotNull SqlDataManager dataManager, @NotNull Key provider, @NotNull String identifier) {
		this.dataManager = dataManager;
		this.provider = provider;
		this.identifier = identifier;
	}

	@Override
	public @NotNull Key provider() {
		return provider;
	}

	@Override
	public @NotNull String identifier() {
		return identifier;
	}

	protected void load(@NotNull ResultSet result) throws SQLException, DataLoadingException {
		for (SavableData<?> dataColumn : dataManager.questerManager.getSavableData()) {
			super.additionalData.put(dataColumn, SQLDataSaver.getFromResultSet(dataColumn, result));
		}

		try (var connection = dataManager.getDbConnection();
				var statement = connection.prepareStatement(dataManager.getSqlHandler().getQuestsData)) {
			fillInIdentifier(statement, 1);
			result = statement.executeQuery();

			while (result.next()) {
				int questId = result.getInt("quest_id");
				try {
					var questData = new QuestData(questId);
					questData.load(result);
					super.questData.put(questId, questData);
				} catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to load quester {} data for quest {}", ex,
							identifier, questId);
				}
			}
		}
		try (var connection = dataManager.getDbConnection();
				var statement = connection.prepareStatement(dataManager.getSqlHandler().getPoolsData)) {
			fillInIdentifier(statement, 1);
			result = statement.executeQuery();

			while (result.next()) {
				int poolId = result.getInt("pool_id");
				try {
					var poolData = new PoolData(poolId);
					poolData.load(result);
					super.poolData.put(poolId, poolData);
				} catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to load quester {} data for pool {}", ex,
							identifier, poolId);
				}
			}
		}
	}

	@Override
	public void save() throws DataSavingException {
		// nothing to do: updates are made in real-time
	}

	@Override
	public void unload() {
		// nothing is really loaded
	}

	@Override
	protected QuesterQuestData createQuestData(@NotNull Quest quest) {
		dataManager.getDataExecutor().execute(() -> {
			try (var connection = dataManager.getDbConnection();
					var statement = connection.prepareStatement(dataManager.getSqlHandler().insertQuestData)) {
				int i = 1;
				i = fillInIdentifier(statement, i);
				statement.setInt(i++, quest.getId());
				statement.executeUpdate();
			} catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"An error occurred while creating quest {} data for {}", ex, quest.getId(), identifier);
			}
		});
		return new QuestData(quest.getId());
	}

	@Override
	protected QuesterPoolData createPoolData(@NotNull QuestPool pool) {
		dataManager.getDataExecutor().execute(() -> {
			try (var connection = dataManager.getDbConnection();
					var statement = connection.prepareStatement(dataManager.getSqlHandler().insertPoolData)) {
				int i = 1;
				i = fillInIdentifier(statement, i);
				statement.setInt(i++, pool.getId());
				statement.executeUpdate();
			} catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"An error occurred while creating pool {} data for {}", ex, pool.getId(), identifier);
			}
		});
		return new PoolData(pool.getId());
	}

	@Override
	protected <T> CompletableFuture<Void> setDataInternal(@NotNull SavableData<T> data, @Nullable T value) {
		return CompletableFuture.runAsync(() -> {
			String setDataSql = dataManager.getSqlHandler().setQuesterAdditionalData.formatted(data.getColumnName());
			try (var connection = dataManager.getDbConnection();
					var statement = connection.prepareStatement(setDataSql)) {
				SQLDataSaver.setInStatement(data, statement, 1, value);
				fillInIdentifier(statement, 2);
				statement.executeUpdate();
			} catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to set data {} for quester {} {}", ex,
						data.getColumnName(), provider, identifier);
				throw new CompletionException(ex);
			}
		}, dataManager.getDataExecutor());
	}

	@Override
	public @NotNull CompletableFuture<Void> delete() {
		return CompletableFuture.runAsync(() -> {
			try (var connection = dataManager.getDbConnection();
					var statement = connection.prepareStatement(dataManager.getSqlHandler().deleteAccount)) {
				fillInIdentifier(statement, 1);
			} catch (SQLException ex) {
				SqlDataManager.LOGGER.severe("Failed to delete data of quester {} {}", ex, provider, identifier);
				throw new CompletionException(ex);
			}
		}, dataManager.getDataExecutor());
	}

	protected int fillInIdentifier(PreparedStatement statement, int i) throws SQLException {
		statement.setString(i++, provider.asString());
		statement.setString(i++, identifier);
		return i;
	}

	protected static void fillInOptionalInt(PreparedStatement statement, int i, OptionalInt value) throws SQLException {
		if (value.isEmpty())
			statement.setNull(i, Types.INTEGER);
		else
			statement.setInt(i, value.getAsInt());
	}

	protected static void fillInOptionalLong(PreparedStatement statement, int i, OptionalLong value) throws SQLException {
		if (value.isEmpty())
			statement.setNull(i, Types.BIGINT);
		else
			statement.setLong(i, value.getAsLong());
	}

	protected static void fillInSerializable(PreparedStatement statement, int i, Object object) throws SQLException {
		statement.setObject(i, CustomizedObjectTypeAdapter.serializeNullable(object));
	}

	private OptionalInt readOptionalInt(@NotNull ResultSet result, String column) throws SQLException {
		int v = result.getInt(column);
		return result.wasNull() ? OptionalInt.empty() : OptionalInt.of(v);
	}

	private OptionalLong readOptionalLong(@NotNull ResultSet result, String column) throws SQLException {
		long v = result.getLong(column);
		return result.wasNull() ? OptionalLong.empty() : OptionalLong.of(v);
	}

	class QuestData extends AbstractQuesterQuestDataImplementation {

		public QuestData(int questID) {
			super(questID);
		}

		protected void load(ResultSet result) throws SQLException, DataLoadingException {
			super.branch = readOptionalInt(result, "current_branch");
			super.stage = readOptionalInt(result, "current_stage");
			super.finished = result.getInt("finished");
			super.timer = readOptionalLong(result, "timer");
			super.startingTime = readOptionalLong(result, "starting_time");

			String flow = result.getString("quest_flow");
			if (flow != null && !flow.isEmpty() && getQuest() != null)
				for (String flowPart : flow.split(";")) {
					super.questFlow.add(StageIndex.fromString(flowPart));
				}

			String state = result.getString("state");
			if (state == null)
				super.migrateState();
			else
				super.state = State.valueOf(state);

			var additionalData =
					CustomizedObjectTypeAdapter.deserializeNullable(result.getString("additional_datas"), Map.class);
			if (additionalData != null) {
				// TODO remove migration 2.0
				super.additionalData = additionalData;
				if (additionalData.containsKey("starting_time")) {
					setStartingTime(OptionalLong.of((long) additionalData.get("starting_time")));
					setAdditionalData("starting_time", null);
				}
			}

			var stageData =
					CustomizedObjectTypeAdapter.deserializeNullable(result.getString("stage_data"), Map.class);
			if (stageData == null) {
				// TODO remove migration 2.0

				var stageDataPattern = Pattern.compile("stage(\\d+)");
				for (var entry : new HashMap<>(super.additionalData).entrySet()) {
					var matcher = stageDataPattern.matcher(entry.getKey());
					if (matcher.matches()) {
						if (entry.getValue() instanceof Map data) {
							this.setAdditionalData(entry.getKey(), null);
							this.setStageData(Integer.parseInt(matcher.group(1)), data);
						} else {
							throw new DataLoadingException("Data in wrong format");
						}
					}
				}
			} else {
				super.stageData = stageData;
			}
		}

		@Override
		public CompletableFuture<Void> remove() {
			return CompletableFuture.runAsync(() -> {
				try (var connection = dataManager.getDbConnection();
						var statement = connection.prepareStatement(dataManager.getSqlHandler().removeQuestData)) {
					int i = 1;
					i = fillInIdentifier(statement, i);
					statement.setInt(i++, questId);
					statement.executeUpdate();
				} catch (SQLException ex) {
					throw new CompletionException(ex);
				}
			}, dataManager.getDataExecutor());
		}

		@Override
		public void setBranch(@NotNull OptionalInt branch) {
			super.setBranch(branch);
			setDataInStatement((statement, i) -> fillInOptionalInt(statement, i, branch), "current_branch");
		}

		@Override
		public void setStage(@NotNull OptionalInt stage) {
			super.setStage(stage);
			setDataInStatement((statement, i) -> fillInOptionalInt(statement, i, stage), "current_stage");
		}

		@Override
		public void setTimesFinished(int times) {
			super.setTimesFinished(times);
			setDataInStatement((statement, i) -> statement.setInt(i, finished), "finished");
		}

		@Override
		public void setStartingTime(@NotNull OptionalLong time) {
			super.setStartingTime(time);
			setDataInStatement((statement, i) -> fillInOptionalLong(statement, i, time), "starting_time");
		}

		@Override
		public void setTimer(@NotNull OptionalLong timer) {
			super.setTimer(timer);
			setDataInStatement((statement, i) -> fillInOptionalLong(statement, i, timer), "timer");
		}

		@Override
		public void addQuestFlow(StageController finished) {
			super.addQuestFlow(finished);
			updatedQuestFlow();
		}

		@Override
		public void resetQuestFlow() {
			super.resetQuestFlow();
			updatedQuestFlow();
		}

		protected void updatedQuestFlow() {
			var flowString = super.questFlow.stream().map(StageIndex::toString).collect(Collectors.joining(";"));
			setDataInStatement((statement, i) -> statement.setString(i, flowString), "quest_flow");
		}

		@Override
		public void setState(@NotNull State state) {
			super.setState(state);
			setDataInStatement((statement, i) -> statement.setString(i, state.name()), "state");
		}

		@Override
		public <T> T setAdditionalData(String key, T value) {
			T o = super.setAdditionalData(key, value);
			setDataInStatement((statement, i) -> fillInSerializable(statement, i, super.additionalData), "additional_datas");
			return o;
		}

		@Override
		public void setStageData(int stage, Map<String, Object> datas) {
			super.setStageData(stage, datas);
			setDataInStatement((statement, i) -> fillInSerializable(statement, i, super.stageData), "stage_data");
		}

		protected void setDataInStatement(StatementSetter setter, String column) {
			// This relies on the fact that [dataManager.getDataExecutor] works as a FIFO.
			// Otherwise, data can be set out of order, and thus consistency is dead
			// (e.g. data is modified back to back and the old data is set last)
			dataManager.getDataExecutor().execute(() -> {
				try (var connection = dataManager.getDbConnection();
						var statement =
								connection.prepareStatement(dataManager.getSqlHandler().getQuestDataStatement(column))) {
					int i = 1;
					setter.accept(statement, i++);
					i = fillInIdentifier(statement, i);
					statement.setInt(i++, questId);
					statement.executeUpdate();
				} catch (SQLException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe(
							"An error occurred while updating {} for {} in quest {}", ex, column, identifier, questId);
				}
			});
		}

	}

	class PoolData extends AbstractQuesterPoolDataImplementation {

		public PoolData(int poolId) {
			super(poolId);
		}

		protected void load(ResultSet result) throws SQLException, DataLoadingException {
			super.lastGive = result.getLong("last_give");

			String completed = result.getString("completed_quests");
			if (completed != null && !completed.isEmpty())
				this.completedQuests =
						Arrays.stream(completed.split(";")).map(Integer::parseInt).collect(Collectors.toSet());
		}

		@Override
		public @NotNull CompletableFuture<Void> remove() {
			return CompletableFuture.runAsync(() -> {
				try (var connection = dataManager.getDbConnection();
						var statement = connection.prepareStatement(dataManager.getSqlHandler().removePoolData)) {
					int i = 1;
					i = fillInIdentifier(statement, i);
					statement.setInt(i++, poolId);
					statement.executeUpdate();
				} catch (SQLException ex) {
					throw new CompletionException(ex);
				}
			}, dataManager.getDataExecutor());
		}

		@Override
		public void setLastGive(long lastGive) {
			super.setLastGive(lastGive);
			setDataInStatement((statement, i) -> statement.setLong(i, lastGive), "last_give");
		}

		@Override
		public void setCompletedQuests(Set<Integer> completedQuests) {
			super.setCompletedQuests(completedQuests);
			var completedQuestsStr = completedQuests.stream().map(String::valueOf).collect(Collectors.joining(";"));
			setDataInStatement((statement, i) -> statement.setString(i, completedQuestsStr), "completed_quests");
		}

		protected void setDataInStatement(StatementSetter setter, String column) {
			dataManager.getDataExecutor().execute(() -> {
				try (var connection = dataManager.getDbConnection();
						var statement =
								connection.prepareStatement(dataManager.getSqlHandler().getPoolDataStatement(column))) {
					int i = 1;
					setter.accept(statement, i++);
					i = fillInIdentifier(statement, i);
					statement.setInt(i++, poolId);
					statement.executeUpdate();
				} catch (SQLException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe(
							"An error occurred while updating {} for {} in pool {}", ex, column, identifier, poolId);
				}
			});
		}

	}

	private interface StatementSetter {
		void accept(PreparedStatement statement, int parameterIndex) throws SQLException;
	}

}
