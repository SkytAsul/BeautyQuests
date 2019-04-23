package fr.skytasul.quests.utils.compatibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import me.clip.placeholderapi.PlaceholderHook;

public class PlaceholderAPI {
	
	public static String setPlaceholders(Player p, String text){
		//DebugUtils.debugMessage(null, me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, text));
		return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, text);
	}
	
	static void registerPlaceholders(){
		me.clip.placeholderapi.PlaceholderAPI.registerPlaceholderHook("BeautyQuests", new PlaceholderHook() {
			Map<Player, List<Quest>> ordered = new HashMap<>();
			Map<Player, List<String>> split = new HashMap<>();
			
			public synchronized String onPlaceholderRequest(Player p, String identifier) {
				if (identifier.equals("total_amount")) return "" + BeautyQuests.getInstance().getQuests().size();
				PlayerAccount acc = PlayersManager.getPlayerAccount(p);
				if (identifier.equals("player_inprogress_amount")) return "" + QuestsAPI.getQuestsStarteds(acc).size();
				if (identifier.equals("player_finished_amount")) return "" + QuestsAPI.getQuestsFinished(acc).size();
				
				if (identifier.startsWith("started_ordered")){
					String after = identifier.substring(15);
					try{
						Quest qu = null;
						String desc = null;
						if (after.isEmpty() || after.equals("_1")){
							if (!ordered.containsKey(p)) ordered.put(p, QuestsAPI.getQuestsStarteds(acc));
							List<Quest> left = ordered.get(p);
							if (left.isEmpty()) return Lang.SCOREBOARD_NONE.toString();
							while(!(qu = left.get(0)).hasStarted(acc)){
								left.remove(0);
							}
							left.remove(0);
							if (left.isEmpty()) ordered.remove(p);
							desc = qu.getStageManager().getDescriptionLine(acc, Source.PLACEHOLDER);
						}

						if (after.isEmpty()){
							return "§6" + qu.getName() + " §e: §o" + desc;
						}else {
							int i = Integer.parseInt(after.substring(1));
							if (i > 1){
								if (QuestsConfiguration.getMaxSplittedAdvancementPlaceholder() < i) return "§cConfig too low";
								List<String> ls = split.get(p);
								if (ls != null) {
									if (ls.size() <= i-2) return "";
									return ls.get(i - 2);
								}
								return "§c§lError";
							}else{
								split.put(p, Utils.splitOnSpace(desc, (QuestsConfiguration.getMaxSplittedAdvancementPlaceholder() - 1)*25));
								return "§6" + qu.getName();
							}
						}
						// /papi parse me %beautyquests_started_ordered_2%
						
						/*AbstractStage stage = qu.getStageManager().getPlayerStage(acc);
						return "§6" + qu.getName() + " §e: §o" + (stage == null ? "finishing" : stage.getDescriptionLine(acc));*/
					}catch (Throwable ex){
						ordered.remove(p);
						split.remove(p);
					}
					return "";
				}
				
				if (identifier.startsWith("advancement_")){
					String sid = identifier.substring(12);
					try{
						Quest qu = QuestsAPI.getQuestFromID(Integer.parseInt(sid));
						if (qu == null) return "§c§lError: unknown quest §o" + sid;
						if (qu.hasStarted(acc)){
							return qu.getStageManager().getDescriptionLine(acc, Source.PLACEHOLDER);

							/*AbstractStage stage = qu.getStageManager().getPlayerStage(acc);
							return stage == null ? "§ofinishing" : stage.getDescriptionLine(acc);*/
						}
						if (qu.hasFinished(acc)) return Lang.Finished.toString();
						return Lang.Not_Started.toString();
					}catch (NumberFormatException ex){
						return "§c§lError: §o" + sid;
					}
				}
				return null;
			}
		});
		BeautyQuests.getInstance().getLogger().info("Placeholders registereds !");
	}
	
}
