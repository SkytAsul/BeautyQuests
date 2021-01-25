package fr.skytasul.quests.utils.compatibility.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BQBossBarImplementation implements BQBossBar {
	
	protected BossBar bar;
	
	public BQBossBarImplementation(String name, String color, String style) {
		this(name, BarColor.valueOf(color), BarStyle.valueOf(style));
	}
	
	public BQBossBarImplementation(String name, BarColor color, BarStyle style) {
		bar = Bukkit.createBossBar(name, color, style);
	}
	
	@Override
	public void setTitle(String name) {
		bar.setTitle(name);
	}
	
	@Override
	public void setProgress(double progress) {
		bar.setProgress(progress);
	}
	
	@Override
	public void addPlayer(Player player) {
		bar.addPlayer(player);
	}
	
	@Override
	public void removePlayer(Player player) {
		bar.removePlayer(player);
	}
	
	@Override
	public void removeAll() {
		bar.removeAll();
	}
	
}
