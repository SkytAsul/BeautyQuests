package fr.skytasul.quests.scoreboards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.DebugUtils;

public class ScoreboardManager{

	private List<ScoreboardLine> lines = new ArrayList<>();
	private int changeTime = 11;
	private boolean hide = true;
	
	private Map<Player, Scoreboard> scoreboards = new HashMap<>();
	
	public ScoreboardManager(YamlConfiguration config){
		if (!QuestsConfiguration.showScoreboards()) return;
		
		hide = config.getBoolean("quests.hideIfEmpty");
		changeTime = config.getInt("quests.changeTime");
		
		for (Map<?, ?> map : config.getMapList("lines")){
			if (lines.size() == 15){
				BeautyQuests.logger.warning("Limit of 15 scoreboard lines reached - please delete some in scoreboard.yml");
				break;
			}
			try{
				lines.add(ScoreboardLine.deserialize((Map<String, Object>) map));
			}catch (Exception ex){
				ex.printStackTrace();
				continue;
			}
		}
		DebugUtils.logMessage("Registered " + lines.size() + " lines in scoreboard");
		
		new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()){
					try{
						scoreboards.put(p, new Scoreboard(p, ScoreboardManager.this));
					}catch (Throwable ex){
						ex.printStackTrace();
						continue;
					}
				}
				if (!scoreboards.isEmpty()) BeautyQuests.getInstance().getLogger().info(scoreboards.size() + " scoreboards created");
			}
		}.runTaskLater(BeautyQuests.getInstance(), 2L);
	}
	
	public List<ScoreboardLine> getScoreboardLines(){
		return lines;
	}
	
	public int getQuestChangeTime(){
		return changeTime;
	}
	
	public boolean hideEmtptyScoreboard(){
		return hide;
	}
	
	public Scoreboard getPlayerScoreboard(Player p){
		return scoreboards.get(p);
	}
	
	public void removePlayerScoreboard(Player p){
		scoreboards.remove(p).unload();
	}
	
	public void create(Player p){
		if (!QuestsConfiguration.showScoreboards()) return;
		scoreboards.put(p, new Scoreboard(p, this));
	}
	
	public void unload(){
		for (Scoreboard s : scoreboards.values()) s.unload();
		if (!scoreboards.isEmpty()) BeautyQuests.getInstance().getLogger().info(scoreboards.size() + " scoreboards deleted.");
		scoreboards.clear();
	}
	
}
