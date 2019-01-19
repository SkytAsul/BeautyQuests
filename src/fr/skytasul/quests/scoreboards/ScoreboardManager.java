package fr.skytasul.quests.scoreboards;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;

public class ScoreboardManager{
	
	public static final List<Scoreboard> scoreboards = new ArrayList<>();
	
	public static void onJoin(PlayerJoinEvent e){
		if (!QuestsConfiguration.showScoreboards()) return;
		scoreboards.add(new Scoreboard(e.getPlayer()));
	}
	
	public static void initialize(){
		if (!QuestsConfiguration.showScoreboards()) return;
		new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()){
					try{
						scoreboards.add(new Scoreboard(p));
					}catch (Throwable ex){
						ex.printStackTrace();
						continue;
					}
				}
				if (!scoreboards.isEmpty()) BeautyQuests.getInstance().getLogger().info(scoreboards.size() + " scoreboards created");
			}
		}.runTaskLater(BeautyQuests.getInstance(), 2L);
	}
	
	public static void unload(){
		for (Scoreboard s : scoreboards) s.unload();
		if (!scoreboards.isEmpty()) BeautyQuests.getInstance().getLogger().info(scoreboards.size() + " scoreboards deleted.");
		scoreboards.clear();
	}
	
}
