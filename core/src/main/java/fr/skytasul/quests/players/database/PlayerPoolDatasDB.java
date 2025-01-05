package fr.skytasul.quests.players.database;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.players.PlayerQuesterImplementation;
import fr.skytasul.quests.players.PlayerPoolDatasImplementation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class PlayerPoolDatasDB extends PlayerPoolDatasImplementation {

	private final PlayersManagerDB playersManager;

	public PlayerPoolDatasDB(PlayersManagerDB playersManagerDB, PlayerQuesterImplementation acc, int poolID) {
		super(acc, poolID);
		playersManager = playersManagerDB;
	}

	public PlayerPoolDatasDB(PlayersManagerDB playersManagerDB, PlayerQuesterImplementation acc, int poolID, long lastGive, Set<Integer> completedQuests) {
		super(acc, poolID, lastGive, completedQuests);
		playersManager = playersManagerDB;
	}

	@Override
	public void setLastGive(long lastGive) {
		super.setLastGive(lastGive);
		updateData(playersManager.getDatabaseHandler().updatePoolLastGive, lastGive);
	}

	@Override
	public void updatedCompletedQuests() {
		updateData(playersManager.getDatabaseHandler().updatePoolCompletedQuests,
				PlayersManagerDB.getCompletedQuestsString(getCompletedQuests()));
	}

	private void updateData(String dataStatement, Object data) {
		try (Connection connection = playersManager.getDbConnection()) {
			try (PreparedStatement statement =
					connection.prepareStatement(playersManager.getDatabaseHandler().getPoolAccountData)) {
				statement.setInt(1, acc.index);
				statement.setInt(2, poolID);
				if (!statement.executeQuery().next()) { // if result set empty => need to insert data then update
					try (PreparedStatement insertStatement =
							connection.prepareStatement(playersManager.getDatabaseHandler().insertPoolData)) {
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