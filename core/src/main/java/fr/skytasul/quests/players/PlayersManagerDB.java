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
import fr.skytasul.quests.utils.Database.BQStatement;

public class PlayersManagerDB extends PlayersManager {

	private static final String ACCOUNTS_TABLE = "`player_accounts`";
	private static final String DATAS_TABLE = "`player_quests`";

	private Database db;
	private BQStatement getAccounts;
	private BQStatement insertAccount;

	private BQStatement insertQuestData;
	private BQStatement removeQuestData;
	private BQStatement getQuestsData;

	private BQStatement removeExistingQuestDatas;

	private BQStatement updateFinished;
	private BQStatement updateTimer;
	private BQStatement updateBranch;
	private BQStatement updateStage;
	private BQStatement[] updateDatas = new BQStatement[5];

	public PlayersManagerDB(Database db) {
		this.db = db;
	}

	public PlayerAccount retrievePlayerAccount(Player p) {
		try {
			String uuid = p.getUniqueId().toString();
			PreparedStatement statement = getAccounts.getStatement();
			statement.setString(1, uuid);
			ResultSet result = statement.executeQuery();
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
			statement = insertAccount.getStatement();
			statement.setString(1, absacc.getIdentifier());
			statement.setString(2, uuid);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
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
			PreparedStatement statement = getQuestsData.getStatement();
			statement.setInt(1, acc.index);
			ResultSet result = statement.executeQuery();
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

	public PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest) {
		return new PlayerQuestDatasDB(acc, quest.getID());
	}

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest, PlayerQuestDatas datas) {
		try {
			for (Entry<BukkitTask, Object> entry : ((PlayerQuestDatasDB) datas).cachedDatas.values()) {
				entry.getKey().cancel();
			}
			PreparedStatement statement = removeQuestData.getStatement();
			statement.setInt(1, acc.index);
			statement.setInt(2, quest.getID());
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int removeQuestDatas(Quest quest) {
		int amount = 0;
		try {
			PreparedStatement statement = removeExistingQuestDatas.getStatement();
			statement.setInt(1, quest.getID());
			amount += statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return amount;
	}

	public boolean hasAccounts(Player p) {
		try {
			PreparedStatement statement = getAccounts.getStatement();
			statement.setString(1, p.getUniqueId().toString());
			ResultSet result = statement.executeQuery();
			return result.last();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void load() {
		try {
			createTables(db);

			getAccounts = db.new BQStatement("SELECT * FROM " + ACCOUNTS_TABLE + " WHERE `player_uuid` = ?");
			insertAccount = db.new BQStatement("INSERT INTO " + ACCOUNTS_TABLE + " (`identifier`, `player_uuid`) VALUES (?, ?)", true);

			insertQuestData = db.new BQStatement("INSERT INTO " + DATAS_TABLE + " (`account_id`, `quest_id`) VALUES (?, ?)");
			removeQuestData = db.new BQStatement("DELETE FROM " + DATAS_TABLE + " WHERE `account_id` = ? AND `quest_id` = ?");
			getQuestsData = db.new BQStatement("SELECT * FROM " + DATAS_TABLE + " WHERE `account_id` = ?");

			removeExistingQuestDatas = db.new BQStatement("DELETE FROM " + DATAS_TABLE + " WHERE `quest_id` = ?");
			
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

	private BQStatement prepareDatasStatement(String column) throws SQLException {
		return db.new BQStatement("UPDATE " + DATAS_TABLE + " SET `" + column + "` = ? WHERE `account_id` = ? AND `quest_id` = ?");
	}

	private static void createTables(Database db) throws SQLException {
		db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE + " ("
				+ " `id` int NOT NULL AUTO_INCREMENT ,"
				+ " `identifier` text NOT NULL ,"
				+ " `player_uuid` char(36) NOT NULL ,"
				+ " PRIMARY KEY (`id`)"
				+ " )");
		db.getStatement().execute("CREATE TABLE IF NOT EXISTS " + DATAS_TABLE + " (" +
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
	}

	public static String migrate(Database db, PlayersManagerYAML yaml) throws SQLException {
		ResultSet result = db.getStatement().getConnection().getMetaData().getTables(null, null, "%", null);
		while (result.next()) {
			String tableName = result.getString(3);
			if (tableName.equals("player_accounts") || tableName.equals("player_quests")) {
				result.close();
				return "§cTable \"" + tableName + "\" already exists. Please drop it before migration.";
			}
		}
		result.close();

		createTables(db);

		PreparedStatement insertAccount = db.prepareStatement("INSERT INTO " + ACCOUNTS_TABLE + " (`id`, `identifier`, `player_uuid`) VALUES (?, ?, ?)");
		PreparedStatement insertQuestData = db.prepareStatement("INSERT INTO " + DATAS_TABLE + " (`account_id`, `quest_id`, `finished`, `timer`, `current_branch`, `current_stage`, `stage_0_datas`, `stage_1_datas`, `stage_2_datas`, `stage_3_datas`, `stage_4_datas`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		int amount = 0;
		yaml.loadAllAccounts();
		for (PlayerAccount acc : yaml.loadedAccounts.values()) {
			insertAccount.setInt(1, acc.index);
			insertAccount.setString(2, acc.abstractAcc.getIdentifier());
			insertAccount.setString(3, acc.getOfflinePlayer().getUniqueId().toString());
			insertAccount.executeUpdate();

			for (Entry<Integer, PlayerQuestDatas> entry : acc.datas.entrySet()) {
				insertQuestData.setInt(1, acc.index);
				insertQuestData.setInt(2, entry.getKey());
				insertQuestData.setBoolean(3, entry.getValue().isFinished());
				insertQuestData.setLong(4, entry.getValue().getTimer());
				insertQuestData.setInt(5, entry.getValue().getBranch());
				insertQuestData.setInt(6, entry.getValue().getStage());
				for (int i = 0; i < 5; i++) {
					Map<String, Object> stageDatas = entry.getValue().getStageDatas(i);
					insertQuestData.setString(7 + i, stageDatas == null ? null : CustomizedObjectTypeAdapter.GSON.toJson(stageDatas));
				}
				insertQuestData.executeUpdate();
			}
			amount++;
		}

		return "§aMigration succeed! " + amount + " accounts migrated.\n§oDatabase saving system is §lnot§r§a§o enabled. You need to reboot the server with the line \"database.enabled\" set to true.";
	}

	public void save() {}

	public class PlayerQuestDatasDB extends PlayerQuestDatas {

		private Map<BQStatement, Entry<BukkitTask, Object>> cachedDatas = new HashMap<>(5);

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

		private void setDataStatement(BQStatement dataStatement, Object data) {
			if (cachedDatas.containsKey(dataStatement)){
				cachedDatas.get(dataStatement).setValue(data);
				return;
			}
			cachedDatas.put(dataStatement, new AbstractMap.SimpleEntry<>(Bukkit.getScheduler().runTaskLaterAsynchronously(BeautyQuests.getInstance(), () -> {
				try {
					Entry<BukkitTask, Object> entry = cachedDatas.remove(dataStatement);
					synchronized (dataStatement) {
						PreparedStatement statement = dataStatement.getStatement();
						statement.setObject(1, entry.getValue());
						statement.setInt(2, acc.index);
						statement.setInt(3, questID);
						if (statement.executeUpdate() == 0) {
							PreparedStatement insertStatement = insertQuestData.getStatement();
							insertStatement.setInt(1, acc.index);
							insertStatement.setInt(2, questID);
							insertStatement.executeUpdate();
							statement.executeUpdate();
						}
					}
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}, 20L), data));
		}

	}

}
