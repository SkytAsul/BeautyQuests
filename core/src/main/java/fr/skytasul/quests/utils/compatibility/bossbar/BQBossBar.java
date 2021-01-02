package fr.skytasul.quests.utils.compatibility.bossbar;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.nms.NMS;

public interface BQBossBar {
	
	void setTitle(String name);
	
	void setProgress(double progress);
	
	void addPlayer(Player player);
	
	void removePlayer(Player player);
	
	void removeAll();
	
	public static final boolean BARS_ENABLED = NMS.getMCVersion() >= 9;
	
}
