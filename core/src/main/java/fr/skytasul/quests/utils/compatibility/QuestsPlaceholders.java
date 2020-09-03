package fr.skytasul.quests.utils.compatibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class QuestsPlaceholders extends PlaceholderExpansion {
	
	Map<Player, List<Quest>> ordered = new HashMap<>();
	Map<Player, List<String>> split = new HashMap<>();
	
	private QuestsPlaceholders() {}
	
	public static String setPlaceholders(OfflinePlayer p, String text) {
		return PlaceholderAPI.setPlaceholders(p, text);
	}
	
	static void registerPlaceholders(){
		new QuestsPlaceholders().register();
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
	public String onRequest(OfflinePlayer off, String identifier) {
		if (identifier.equals("total_amount")) return "" + BeautyQuests.getInstance().getQuests().size();
		if (!off.isOnline()) return "§cerror: offline";
		Player p = off.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (identifier.equals("player_inprogress_amount")) return "" + QuestsAPI.getQuestsStarteds(acc).size();
		if (identifier.equals("player_finished_amount")) return "" + QuestsAPI.getQuestsFinished(acc).size();
		
		if (identifier.startsWith("started_ordered")) {
			String after = identifier.substring(15);
			try {
				Quest qu = null;
				String desc = null;
				if (after.isEmpty() || after.equals("_1")) {
					if (!ordered.containsKey(p)) ordered.put(p, QuestsAPI.getQuestsStarteds(acc, true));
					List<Quest> left = ordered.get(p);
					QuestsAPI.updateQuestsStarteds(acc, true, left);
					if (left.isEmpty()) {
						split.remove(p);
						return Lang.SCOREBOARD_NONE.toString();
					}
					while (!(qu = left.get(0)).hasStarted(acc)) {
						left.remove(0);
					}
					left.remove(0);
					if (left.isEmpty()) ordered.remove(p);
					desc = qu.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.PLACEHOLDER);
				}
				
				if (after.isEmpty()) {
					return "§6" + qu.getName() + " §e: §o" + desc;
				}
				int i = Integer.parseInt(after.substring(1));
				if (i > 1) {
					if (QuestsConfiguration.getMaxSplittedAdvancementPlaceholder() < i) return "§cConfig too low";
					List<String> ls = split.get(p);
					if (ls != null && ls.size() > i - 2) return ls.get(i - 2);
					return "";
				}
				split.put(p, Utils.wordWrap(desc, (QuestsConfiguration.getMaxSplittedAdvancementPlaceholder() - 1) * 25));
				return "§6" + qu.getName();
				
				/*AbstractStage stage = qu.getStageManager().getPlayerStage(acc);
				return "§6" + qu.getName() + " §e: §o" + (stage == null ? "finishing" : stage.getDescriptionLine(acc));*/
			}catch (Exception ex) {
				ordered.remove(p);
				split.remove(p);
				return ex.getMessage();
			}
		}
		
		if (identifier.startsWith("advancement_")) {
			String sid = identifier.substring(12);
			try {
				Quest qu = QuestsAPI.getQuestFromID(Integer.parseInt(sid));
				if (qu == null) return "§c§lError: unknown quest §o" + sid;
				if (qu.hasStarted(acc)) {
					return qu.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.PLACEHOLDER);
					
					/*AbstractStage stage = qu.getStageManager().getPlayerStage(acc);
					return stage == null ? "§ofinishing" : stage.getDescriptionLine(acc);*/
				}
				if (qu.hasFinished(acc)) return Lang.Finished.toString();
				return Lang.Not_Started.toString();
			}catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid;
			}
		}
		return null;
	}
	
}
