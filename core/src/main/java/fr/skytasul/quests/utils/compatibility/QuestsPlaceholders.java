package fr.skytasul.quests.utils.compatibility;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class QuestsPlaceholders extends PlaceholderExpansion {
	
	private final int lineLength, changeTime;
	private BukkitTask task;
	private Map<Player, PlayerPlaceholderData> players = new HashMap<>();
	private ReentrantLock playersLock = new ReentrantLock();
	
	private QuestsPlaceholders(ConfigurationSection placeholderConfig) {
		lineLength = placeholderConfig.getInt("lineLength");
		changeTime = placeholderConfig.getInt("changeTime");
	}
	
	public static String setPlaceholders(OfflinePlayer p, String text) {
		return PlaceholderAPI.setPlaceholders(p, text);
	}
	
	static void registerPlaceholders(ConfigurationSection placeholderConfig) {
		new QuestsPlaceholders(placeholderConfig).register();
		BeautyQuests.getInstance().getLogger().info("Placeholders registereds !");
	}
	
	@Override
	public String getAuthor() {
		return BeautyQuests.getInstance().getDescription().getAuthors().toString();
	}
	
	@Override
	public String getIdentifier() {
		return "beautyquests";
	}
	
	@Override
	public String getVersion() {
		return BeautyQuests.getInstance().getDescription().getVersion();
	}
	
	@Override
	public boolean persist() {
		return true;
	}
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public List<String> getPlaceholders() {
		return Arrays.asList("total_amount", "player_inprogress_amount", "player_finished_amount", "started_ordered");
	}
	
	@Override
	public String onRequest(OfflinePlayer off, String identifier) {
		if (identifier.equals("total_amount")) return "" + BeautyQuests.getInstance().getQuests().size();
		if (!off.isOnline()) return "§cerror: offline";
		Player p = off.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (acc == null) return "§cdatas not loaded";
		if (identifier.equals("player_inprogress_amount")) return "" + QuestsAPI.getQuestsStarteds(acc).size();
		if (identifier.equals("player_finished_amount")) return "" + QuestsAPI.getQuestsFinished(acc, false).size();
		if (identifier.equals("player_finished_total_amount")) return "" + acc.getQuestsDatas().stream().mapToInt(PlayerQuestDatas::getTimesFinished).sum();
		
		if (identifier.startsWith("started_ordered")) {
			String after = identifier.substring(15);
			if (task == null) launchTask();
			
			playersLock.lock();
			try {
				PlayerPlaceholderData data = players.get(p);
				
				if (data == null) {
					data = new PlayerPlaceholderData(acc);
					players.put(p, data);
				}
				
				if (data.left.isEmpty()) {
					data.left = QuestsAPI.getQuestsStarteds(data.acc, true);
				}else QuestsAPI.updateQuestsStarteds(acc, true, data.left);
				
				try {
					int i = -1;
					if (!after.isEmpty()) {
						i = Integer.parseInt(after.substring(1)) - 1;
						if (i < 0) return "§cindex must be positive";
					}
					
					if (data.left.isEmpty()) return i == -1 || i == 0 ? Lang.SCOREBOARD_NONE.toString() : "";
					
					Quest quest = data.left.get(0);
					String desc = quest.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.PLACEHOLDER);
					if (after.isEmpty()) return desc;
				
					try {
						List<String> lines = Utils.wordWrap(desc, lineLength);
						if (i >= lines.size()) return "";
						return lines.get(i);
					}catch (Exception ex) {
						players.remove(p);
						return "§c" + ex.getMessage();
					}
				}catch (Exception ex) {
					return "§cinvalid placeholder";
				}
			}finally {
				playersLock.unlock();
			}
		}
		
		if (identifier.startsWith("advancement_")) {
			String sid = identifier.substring(12);
			try {
				Quest qu = QuestsAPI.getQuestFromID(Integer.parseInt(sid));
				if (qu == null) return "§c§lError: unknown quest §o" + sid;
				if (qu.hasStarted(acc)) {
					return qu.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.PLACEHOLDER);
				}
				if (qu.hasFinished(acc)) return Lang.Finished.toString();
				return Lang.Not_Started.toString();
			}catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid;
			}
		}
		return null;
	}
	
	private void launchTask() {
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(BeautyQuests.getInstance(), () -> {
			playersLock.lock();
			try {
				for (Iterator<Entry<Player, PlayerPlaceholderData>> iterator = players.entrySet().iterator(); iterator.hasNext();) {
					Entry<Player, PlayerPlaceholderData> entry = iterator.next();
					if (!entry.getKey().isOnline()) {
						iterator.remove();
						continue;
					}
					PlayerPlaceholderData data = entry.getValue();
					if (!data.left.isEmpty()) data.left.remove(0);
				}
			}finally {
				playersLock.unlock();
			}
		}, 0, changeTime * 20);
	}
	
	class PlayerPlaceholderData {
		private List<Quest> left = Collections.EMPTY_LIST;
		private PlayerAccount acc;
		
		public PlayerPlaceholderData(PlayerAccount acc) {
			this.acc = acc;
		}
	}
	
}
