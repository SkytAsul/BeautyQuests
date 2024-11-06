package fr.skytasul.quests.players.database;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.players.DataException;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import org.bukkit.scheduler.BukkitRunnable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PlayerQuestDatasDB extends PlayerQuestDatasImplementation {

	private final PlayersManagerDB playersManager;

	private static final int DATA_QUERY_TIMEOUT = 15;
	private static final int DATA_FLUSHING_TIME = 10;

	private Map<String, Entry<BukkitRunnable, Object>> cachedDatas = new HashMap<>(5);
	private Lock datasLock = new ReentrantLock();
	private Lock dbLock = new ReentrantLock();
	private boolean disabled = false;
	private int dbId = -1;

	public PlayerQuestDatasDB(PlayersManagerDB playersManagerDB, PlayerAccountImplementation acc, int questID) {
		super(acc, questID);
		playersManager = playersManagerDB;
	}

	public PlayerQuestDatasDB(PlayersManagerDB playersManagerDB, PlayerAccountImplementation acc, int questID, ResultSet result) throws SQLException {
		super(
				acc,
				questID,
				result.getLong("timer"),
				result.getInt("finished"),
				result.getInt("current_branch"),
				result.getInt("current_stage"),
				CustomizedObjectTypeAdapter.deserializeNullable(result.getString("additional_datas"), Map.class),
				result.getString("quest_flow"));
		playersManager = playersManagerDB;
		this.dbId = result.getInt("id");
	}

	@Override
	public void incrementFinished() {
		super.incrementFinished();
		setDataStatement(playersManager.getDatabaseHandler().updateFinished, getTimesFinished(), false);
	}

	@Override
	public void setTimer(long timer) {
		super.setTimer(timer);
		setDataStatement(playersManager.getDatabaseHandler().updateTimer, timer, false);
	}

	@Override
	public void setBranch(int branch) {
		super.setBranch(branch);
		setDataStatement(playersManager.getDatabaseHandler().updateBranch, branch, false);
	}

	@Override
	public void setStage(int stage) {
		super.setStage(stage);
		setDataStatement(playersManager.getDatabaseHandler().updateStage, stage, false);
	}

	@Override
	public <T> T setAdditionalData(String key, T value) {
		T additionalData = super.setAdditionalData(key, value);
		setDataStatement(playersManager.getDatabaseHandler().updateDatas, super.additionalDatas.isEmpty() ? null
				: CustomizedObjectTypeAdapter.serializeNullable(super.additionalDatas), true);
		return additionalData;
	}

	@Override
	public void addQuestFlow(StageController finished) {
		super.addQuestFlow(finished);
		setDataStatement(playersManager.getDatabaseHandler().updateFlow, getQuestFlow(), true);
	}

	@Override
	public void resetQuestFlow() {
		super.resetQuestFlow();
		setDataStatement(playersManager.getDatabaseHandler().updateFlow, null, true);
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
				BukkitRunnable runnable = new BukkitRunnable() {

					@Override
					public void run() {
						if (disabled) return;
						Entry<BukkitRunnable, Object> entry = null;
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
									try (Connection connection = PlayerQuestDatasDB.this.playersManager.getDbConnection()) {
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
					}
				};
				runnable.runTaskLaterAsynchronously(BeautyQuests.getInstance(), DATA_FLUSHING_TIME);
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
							run.cancel();
							run.run();
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
			.forEach(BukkitRunnable::cancel);
		cachedDatas.clear();
		datasLock.unlock();
	}

	private void createDataRow(Connection connection) throws SQLException {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Inserting DB row of quest " + questID + " for account " + acc.index);
		try (PreparedStatement insertStatement =
				connection.prepareStatement(playersManager.getDatabaseHandler().insertQuestData, new String[] {"id"})) {
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