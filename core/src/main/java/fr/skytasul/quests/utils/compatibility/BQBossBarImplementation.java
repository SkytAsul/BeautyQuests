package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.BossBarManager;

public class BQBossBarImplementation implements BossBarManager {
	
	@Override
	public BQBossBar buildBossBar(String name, BarColor color, BarStyle style) {
		return new BarImpl(name, color, style);
	}
	
	private class BarImpl implements BossBarManager.BQBossBar {
		
		protected BossBar bar;
		
		public BarImpl(String name, BarColor color, BarStyle style) {
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
		public void setStyle(BarStyle style) {
			bar.setStyle(style);
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
	
}
