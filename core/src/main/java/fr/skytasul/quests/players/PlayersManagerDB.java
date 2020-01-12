package fr.skytasul.quests.players;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.Utils;

public class PlayersManagerDB extends PlayersManager {

	private final String accountsTable;
	private final String datasTable;

	private Database db;
	private PreparedStatement getAccounts;
	private PreparedStatement insertAccount;
	//private PreparedStatement getIndex;

	private PreparedStatement insertQuestData;
	private PreparedStatement updateQuestData;
	private PreparedStatement removeQuestData;
	private PreparedStatement getQuestsData;

	public PlayersManagerDB(Database db) {
		this.db = db;
		this.accountsTable = db.getDatabase() + ".player_accounts";
		this.datasTable = db.getDatabase() + ".player_quests";
	}

	public PlayerAccount retrievePlayerAccount(Player p) {
		try {
			String uuid = p.getUniqueId().toString();
			getAccounts.setString(0, uuid);
			ResultSet result = getAccounts.executeQuery();
			while (result.next()) {
				AbstractAccount abs = createAccountFromIdentifier(result.getString("identifier"));
				if (abs.isCurrent()) {
					PlayerAccount account = new PlayerAccount(abs, result.getInt("id"));
					Utils.runAsync(() -> retrievePlayerDatas(account));
					return account;
				}
			}

			PlayerAccount acc = new PlayerAccount(super.createAbstractAccount(p), -1);
			insertAccount.setString(0, acc.abstractAcc.getIdentifier());
			insertAccount.setString(1, uuid);
			insertAccount.executeUpdate();
			result = insertAccount.getGeneratedKeys();
			result.last();
			acc.index = result.getInt("id");
			return acc;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void retrievePlayerDatas(PlayerAccount acc) {
		try {
			getQuestsData.setInt(0, acc.index);
			ResultSet result = getQuestsData.executeQuery();
			while (result.next()) {
				int questID = result.getInt("quest_id");
				acc.datas.put(questID, new PlayerQuestDatasDB(acc, questID, result.getBoolean("finished"), result.getLong("timer"), result.getInt("current_branch"), result.getInt("current_stage"), getStageDatas(result, 0), getStageDatas(result, 1), getStageDatas(result, 2), getStageDatas(result, 3), getStageDatas(result, 4)));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> getStageDatas(ResultSet result, int index) throws SQLException {
		String json = result.getString("stage_" + index + "_datas");
		if (json == null) return null;
		return new Gson().fromJson(json, Map.class);
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

	public void dataUpdated(PlayerAccount acc, int questID, String key, Object data) {
		Utils.runAsync(() -> {
			try {
				updateQuestData.setString(0, key);
				updateQuestData.setObject(1, data);
				updateQuestData.setInt(2, acc.index);
				updateQuestData.setInt(3, questID);
				if (updateQuestData.executeUpdate() == 0) {
					insertQuestData.setString(0, key);
					insertQuestData.setInt(1, acc.index);
					insertQuestData.setInt(2, questID);
					insertQuestData.setObject(3, data);
					insertQuestData.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest) {
		try {
			removeQuestData.setInt(0, acc.index);
			removeQuestData.setInt(1, quest.getID());
			removeQuestData.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasAccounts(Player p) {
		try {
			getAccounts.setString(0, p.getUniqueId().toString());
			ResultSet result = getAccounts.executeQuery();
			return result.last();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void load() {
		try {
			db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + accountsTable + "("
					+ " id int NOT NULL AUTO_INCREMENT ,"
					+ " identifier text NOT NULL ,"
					+ " player_uuid char(36) NOT NULL ,"
					+ " PRIMARY KEY (id)"
					+ ")");
			getAccounts = db.prepareStatement("SELECT * FROM " + accountsTable + " WHERE player_uuid = ?");
			insertAccount = db.prepareStatementGeneratedKeys("INSERT INTO " + accountsTable + " (identifier, player_uuid, ?) VALUES (?, ?, ?)");
			//getIndex = db.prepareStatement("SELECT id FROM " + accountsTable + " WHERE identifier = ?");

			db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + datasTable + " (" +
					" account_id int(11) NOT NULL," +
					" quest_id int(11) NOT NULL," +
					" finished tinyint(1) DEFAULT NULL," +
					" timer bigint(20) DEFAULT NULL," +
					" current_branch tinyint(4) DEFAULT NULL," +
					" current_stage tinyint(4) DEFAULT NULL," +
					" stage_0_datas longtext DEFAULT NULL," +
					" stage_1_datas longtext DEFAULT NULL," +
					" stage_2_datas longtext DEFAULT NULL," +
					" stage_3_datas longtext DEFAULT NULL," +
					" stage_4_datas longtext DEFAULT NULL" +
					")");
			insertQuestData = db.prepareStatement("INSERT INTO " + datasTable + " (account_id, quest_id) VALUES (?, ?)");
			updateQuestData = db.prepareStatement("UPDATE " + datasTable + " SET ? = ? WHERE account_id = ? AND quest_id = ?");
			removeQuestData = db.prepareStatement("DELETE FROM " + datasTable + " WHERE account_id = ? AND quest_id = ?");
			getQuestsData = db.prepareStatement("SELECT FROM " + datasTable + " WHERE account_id = ?");
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void save() {}

	public class PlayerQuestDatasDB extends PlayerQuestDatas {

		public PlayerQuestDatasDB(PlayerAccount acc, int questID) {
			super(acc, questID);
		}

		public PlayerQuestDatasDB(PlayerAccount acc, int questID, boolean finished, long timer, int branch, int stage, Map<String, Object> stage0datas, Map<String, Object> stage1datas, Map<String, Object> stage2datas, Map<String, Object> stage3datas, Map<String, Object> stage4datas) {
			super(acc, questID, timer, finished, branch, stage, stage0datas, stage1datas, stage2datas, stage3datas, stage4datas);
		}
		
		public void setFinished(boolean finished) {
			super.setFinished(finished);
			dataUpdated(acc, questID, "finished", finished);
		}
		
		public void setTimer(long timer) {
			super.setTimer(timer);
			dataUpdated(acc, questID, "timer", timer);
		}

		public void setBranch(int branch) {
			super.setBranch(branch);
			dataUpdated(acc, questID, "current_branch", branch);
		}

		public void setStage(int stage) {
			super.setStage(stage);
			dataUpdated(acc, questID, "current_stage", stage);
		}

		public void setStageDatas(int stage, Map<String, Object> stageDatas) {
			super.setStageDatas(stage, stageDatas);
			dataUpdated(acc, questID, "stage_" + stage + "_datas", new Gson().toJson(stageDatas));
		}

	}

}
