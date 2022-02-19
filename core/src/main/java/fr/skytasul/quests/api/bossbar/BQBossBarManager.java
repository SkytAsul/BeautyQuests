package fr.skytasul.quests.api.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public interface BQBossBarManager {
	
	BQBossBar buildBossBar(String name, BarColor color, BarStyle style);
	
	default BQBossBar buildBossBar(String name, String color, String style) {
		return buildBossBar(name, BarColor.valueOf(color), BarStyle.valueOf(style));
	}
	
	public interface BQBossBar {
		
		void setTitle(String name);
		
		void setProgress(double progress);
		
		void setStyle(BarStyle style);
		
		void addPlayer(Player player);
		
		void removePlayer(Player player);
		
		void removeAll();
		
	}
	
}
