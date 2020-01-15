package fr.skytasul.quests.players;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.utils.Database;

public class PlayersManagerDB extends PlayersManager {

	private final String accountsTable;
	private final String datasTable;

	private Database db;
	private PreparedStatement getAccounts;
	private PreparedStatement insertAccount;

	private PreparedStatement insertQuestData;
	private PreparedStatement removeQuestData;
	private PreparedStatement getQuestsData;

	private PreparedStatement updateFinished;
	private PreparedStatement updateTimer;
	private PreparedStatement updateBranch;
	private PreparedStatement updateStage;
	private PreparedStatement[] updateDatas = new PreparedStatement[5];

	public PlayersManagerDB(Database db) {
		this.db = db;
		this.accountsTable = "`player_accounts`";
		this.datasTable = "`player_quests`";
	}

	public PlayerAccount retrievePlayerAccount(Player p) {
		try {
			String uuid = p.getUniqueId().toString();
			getAccounts.setString(1, uuid);
			ResultSet result = getAccounts.executeQuery();
			while (result.next()) {
				AbstractAccount abs = createAccountFromIdentifier(result.getString("identifier"));
				if (abs.isCurrent()) {
					PlayerAccount account = new PlayerAccount(abs, result.getInt("id"));
					result.close();
					retrievePlayerDatas(account);
					return account;
				}
			}

			AbstractAccount absacc = super.createAbstractAccount(p);
			insertAccount.setString(1, absacc.getIdentifier());
			insertAccount.setString(2, uuid);
			insertAccount.executeUpdate();
			result = insertAccount.getGeneratedKeys();
			if (!result.next()) throw new RuntimeException("The plugin has not been able to create a player account.");
			int index = result.getInt(1);
			result.close();
			return new PlayerAccount(absacc, index);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void retrievePlayerDatas(PlayerAccount acc) {
		try {
			getQuestsData.setInt(1, acc.index);
			ResultSet result = getQuestsData.executeQuery();
			while (result.next()) {
				int questID = result.getInt("quest_id");
				acc.datas.put(questID, new PlayerQuestDatasDB(acc, questID, result.getBoolean("finished"), result.getLong("timer"), result.getInt("current_branch"), result.getInt("current_stage"), getStageDatas(result, 0), getStageDatas(result, 1), getStageDatas(result, 2), getStageDatas(result, 3), getStageDatas(result, 4)));
			}
			result.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> getStageDatas(ResultSet result, int index) throws SQLException {
		String json = result.getString("stage_" + index + "_datas");
		if (json == null) return null;
		return CustomizedObjectTypeAdapter.GSON.fromJson(json, Map.class);
	}

	/*public int getAccountIndex(PlayerAccount account) {
		try {
			ResultSet result = getIndex.executeQuery();
			result.next();
			return result.getInt("id");
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}*/

	public PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest) {
		return new PlayerQuestDatasDB(acc, quest.getID());
	}

	/*public void dataUpdated(PlayerAccount acc, int questID, String key, Object data) {
		Utils.runAsync(() -> {
			try {
				updateQuestData.setString(1, key);
				updateQuestData.setObject(2, data);
				updateQuestData.setInt(3, acc.index);
				updateQuestData.setInt(4, questID);
				if (updateQuestData.executeUpdate() == 0) {
					insertQuestData.setInt(1, acc.index);
					insertQuestData.setInt(2, questID);
					insertQuestData.executeUpdate();
					updateQuestData.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}*/

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest) {
		try {
			removeQuestData.setInt(1, acc.index);
			removeQuestData.setInt(2, quest.getID());
			removeQuestData.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasAccounts(Player p) {
		try {
			getAccounts.setString(1, p.getUniqueId().toString());
			ResultSet result = getAccounts.executeQuery();
			return result.last();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void load() {
		try {
			db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + accountsTable + " ("
					+ " `id` int NOT NULL AUTO_INCREMENT ,"
					+ " `identifier` text NOT NULL ,"
					+ " `player_uuid` char(36) NOT NULL ,"
					+ " PRIMARY KEY (`id`)"
					+ " )");
			getAccounts = db.prepareStatement("SELECT * FROM " + accountsTable + " WHERE `player_uuid` = ?");
			insertAccount = db.prepareStatementGeneratedKeys("INSERT INTO " + accountsTable + " (`identifier`, `player_uuid`) VALUES (?, ?)");
			//getIndex = db.prepareStatement("SELECT id FROM " + accountsTable + " WHERE identifier = ?");

			db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + datasTable + " (" +
					" `id` int NOT NULL AUTO_INCREMENT ," +
					" `account_id` int(11) NOT NULL," +
					" `quest_id` int(11) NOT NULL," +
					" `finished` tinyint(1) DEFAULT NULL," +
					" `timer` bigint(20) DEFAULT NULL," +
					" `current_branch` tinyint(4) DEFAULT NULL," +
					" `current_stage` tinyint(4) DEFAULT NULL," +
					" `stage_0_datas` longtext DEFAULT NULL," +
					" `stage_1_datas` longtext DEFAULT NULL," +
					" `stage_2_datas` longtext DEFAULT NULL," +
					" `stage_3_datas` longtext DEFAULT NULL," +
					" `stage_4_datas` longtext DEFAULT NULL," +
					" PRIMARY KEY (`id`)" +
					")");
			insertQuestData = db.prepareStatement("INSERT INTO " + datasTable + " (`account_id`, `quest_id`) VALUES (?, ?)");
			removeQuestData = db.prepareStatement("DELETE FROM " + datasTable + " WHERE `account_id` = ? AND `quest_id` = ?");
			getQuestsData = db.prepareStatement("SELECT * FROM " + datasTable + " WHERE `account_id` = ?");

			updateFinished = prepareDatasStatement("finished");
			updateTimer = prepareDatasStatement("timer");
			updateBranch = prepareDatasStatement("current_branch");
			updateStage = prepareDatasStatement("current_stage");
			for (int i = 0; i < 5; i++) {
				updateDatas[i] = prepareDatasStatement("stage_" + i + "_datas");
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private PreparedStatement prepareDatasStatement(String column) throws SQLException {
		return db.prepareStatement("UPDATE " + datasTable + " SET `" + column + "` = ? WHERE `account_id` = ? AND `quest_id` = ?");
	}

	public void save() {}

	public class PlayerQuestDatasDB extends PlayerQuestDatas {

		private Map<PreparedStatement, Entry<BukkitTask, Object>> cachedDatas = new HashMap<>(5);

		public PlayerQuestDatasDB(PlayerAccount acc, int questID) {
			super(acc, questID);
		}

		public PlayerQuestDatasDB(PlayerAccount acc, int questID, boolean finished, long timer, int branch, int stage, Map<String, Object> stage0datas, Map<String, Object> stage1datas, Map<String, Object> stage2datas, Map<String, Object> stage3datas, Map<String, Object> stage4datas) {
			super(acc, questID, timer, finished, branch, stage, stage0datas, stage1datas, stage2datas, stage3datas, stage4datas);
		}
		
		public void setFinished(boolean finished) {
			super.setFinished(finished);
			setDataStatement(updateFinished, finished);
		}
		
		public void setTimer(long timer) {
			super.setTimer(timer);
			setDataStatement(updateTimer, timer);
		}

		public void setBranch(int branch) {
			super.setBranch(branch);
			setDataStatement(updateBranch, branch);
		}

		public void setStage(int stage) {
			super.setStage(stage);
			setDataStatement(updateStage, stage);
		}

		public void setStageDatas(int stage, Map<String, Object> stageDatas) {
			super.setStageDatas(stage, stageDatas);
			setDataStatement(updateDatas[stage], stageDatas == null ? null : CustomizedObjectTypeAdapter.GSON.toJson(stageDatas));
		}

		private void setDataStatement(PreparedStatement dataStatement, Object data) {
			if (cachedDatas.containsKey(dataStatement)){
				cachedDatas.get(dataStatement).setValue(data);
				return;
			}
			cachedDatas.put(dataStatement, new AbstractMap.SimpleEntry<>(Bukkit.getScheduler().runTaskLaterAsynchronously(BeautyQuests.getInstance(), () -> {
				try {
					Entry<BukkitTask, Object> entry = cachedDatas.remove(dataStatement);
					synchronized (dataStatement) {
						dataStatement.setObject(1, entry.getValue());
						dataStatement.setInt(2, acc.index);
						dataStatement.setInt(3, questID);
						if (dataStatement.executeUpdate() == 0) {
							insertQuestData.setInt(1, acc.index);
							insertQuestData.setInt(2, questID);
							insertQuestData.executeUpdate();
							dataStatement.executeUpdate();
						}
					}
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}, 20L), data));
		}

	}

}
