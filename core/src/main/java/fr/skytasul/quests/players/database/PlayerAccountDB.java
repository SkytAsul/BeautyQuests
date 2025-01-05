package fr.skytasul.quests.players.database;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SQLDataSaver;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.players.DataException;
import fr.skytasul.quests.players.PlayerQuesterImplementation;
import fr.skytasul.quests.players.PlayerPoolDatasImplementation;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerAccountDB extends PlayerQuesterImplementation {

	private final PlayersManagerDB playersManager;

	public PlayerAccountDB(PlayersManagerDB playersManagerDB, AbstractAccount account, int index) {
		super(account, index);
		playersManager = playersManagerDB;
	}

	protected void retrievePlayerDatas() {
		try (Connection connection = playersManager.getDbConnection()) {
			try (PreparedStatement statement =
					connection.prepareStatement(playersManager.getDatabaseHandler().getQuestsData)) {
				statement.setInt(1, index);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					int questID = result.getInt("quest_id");
					questDatas.put(questID, new PlayerQuestDatasDB(playersManager, this, questID, result));
				}
				result.close();
			}
			try (PreparedStatement statement =
					connection.prepareStatement(playersManager.getDatabaseHandler().getPoolData)) {
				statement.setInt(1, index);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					int poolID = result.getInt("pool_id");
					String completedQuests = result.getString("completed_quests");
					if (StringUtils.isEmpty(completedQuests))
						completedQuests = null;
					poolDatas.put(poolID,
							new PlayerPoolDatasDB(playersManager, this, poolID, result.getLong("last_give"),
									completedQuests == null ? new HashSet<>()
											: Arrays.stream(completedQuests.split(";")).map(Integer::parseInt)
													.collect(Collectors.toSet())));
				}
				result.close();
			}
			if (playersManager.getDatabaseHandler().getAccountDatas != null) {
				try (PreparedStatement statement =
						connection.prepareStatement(playersManager.getDatabaseHandler().getAccountDatas)) {
					statement.setInt(1, index);
					ResultSet result = statement.executeQuery();
					result.next();
					for (SQLDataSaver<?> data : playersManager.accountDatas.values()) {
						additionalDatas.put(data.getWrappedData(), data.getFromResultSet(result));
					}
					result.close();
				}
			}
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("An error occurred while fetching account datas of " + debugName(), ex);
		}
	}

	@Override
	protected PlayerQuestDatasImplementation createQuestDatas(@NotNull Quest quest) {
		return new PlayerQuestDatasDB(playersManager, this, quest.getId());
	}

	@Override
	protected CompletableFuture<Void> questDatasRemoved(PlayerQuestDatasImplementation datas) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = playersManager.getDbConnection();
					PreparedStatement statement =
							connection.prepareStatement(playersManager.getDatabaseHandler().removeQuestData)) {
				((PlayerQuestDatasDB) datas).stop();
				statement.setInt(1, datas.getQuester().index);
				statement.setInt(2, datas.getQuestID());
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing player quest data from database.", ex);
			}
		});
	}

	@Override
	protected @Nullable PlayerQuestDatasImplementation removeQuestDatasSilently(int id) {
		return super.removeQuestDatasSilently(id); // for visibility purpose
	}

	@Override
	protected PlayerPoolDatasImplementation createPoolDatas(@NotNull QuestPool pool) {
		return new PlayerPoolDatasDB(playersManager, this, pool.getId());
	}

	@Override
	protected CompletableFuture<Void> poolDatasRemoved(PlayerPoolDatasImplementation datas) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = playersManager.getDbConnection();
					PreparedStatement statement =
							connection.prepareStatement(playersManager.getDatabaseHandler().removePoolData)) {
				statement.setInt(1, index);
				statement.setInt(2, datas.getPoolID());
				statement.executeUpdate();
			} catch (SQLException ex) {
				throw new DataException("An error occurred while removing player quest data from database.", ex);
			}
		});
	}

	@Override
	protected @Nullable PlayerPoolDatasImplementation removePoolDatasSilently(int id) {
		return super.removePoolDatasSilently(id); // for visibility purpose
	}

	@Override
	public <T> void setData(SavableData<T> data, T value) {
		super.setData(data, value);

		SQLDataSaver<T> dataSaver = (SQLDataSaver<T>) playersManager.accountDatas.get(data);
		try (Connection connection = playersManager.getDbConnection();
				PreparedStatement statement = connection.prepareStatement(dataSaver.getUpdateStatement())) {
			dataSaver.setInStatement(statement, 1, value);
			statement.setInt(2, index);
			statement.executeUpdate();
		} catch (SQLException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("An error occurred while saving account data " + data.getId() + " to database", ex);
		}
	}

	@Override
	public void resetDatas() {
		super.resetDatas();

		if (playersManager.getDatabaseHandler().resetAccountDatas != null) {
			try (Connection connection = playersManager.getDbConnection();
					PreparedStatement statement =
							connection.prepareStatement(playersManager.getDatabaseHandler().resetAccountDatas)) {
				statement.setInt(1, index);
				statement.executeUpdate();
			} catch (SQLException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while resetting account " + index + " datas from database", ex);
			}
		}
	}

	@Override
	public void unload() {
		super.unload();

		QuestUtils.runAsync(() -> save(true));
	}

	public void save(boolean stop) {
		questDatas.values()
				.stream()
				.map(PlayerQuestDatasDB.class::cast)
				.forEach(x -> x.flushAll(stop));
	}

}
