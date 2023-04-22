package fr.skytasul.quests.api.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface BQBossBarManager {
	
	@NotNull
	BQBossBar buildBossBar(@NotNull String name, @NotNull BarColor color, @NotNull BarStyle style);
	
	default @NotNull BQBossBar buildBossBar(@NotNull String name, @NotNull String color, @NotNull String style) {
		return buildBossBar(name, BarColor.valueOf(color), BarStyle.valueOf(style));
	}
	
	public interface BQBossBar {
		
		void setTitle(@NotNull String name);
		
		void setProgress(double progress);
		
		void setStyle(@NotNull BarStyle style);
		
		void addPlayer(@NotNull Player player);
		
		void removePlayer(@NotNull Player player);
		
		void removeAll();
		
	}
	
}
