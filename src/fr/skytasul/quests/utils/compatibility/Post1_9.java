package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;

public class Post1_9 {

	public static Object createMobsBar(String questName, int mobsAmount) {
		BarStyle style = BarStyle.SOLID;
		if (mobsAmount % 20 == 0) {
			style = BarStyle.SEGMENTED_20;
		}else if (mobsAmount % 10 == 0) {
			style = BarStyle.SEGMENTED_10;
		}else if (mobsAmount % 12 == 0) {
			style = BarStyle.SEGMENTED_12;
		}else if (mobsAmount % 6 == 0) {
			style = BarStyle.SEGMENTED_6;
		}
		BossBar bar = Bukkit.createBossBar(Lang.MobsProgression.format(questName, 100, 100), BarColor.YELLOW, style);
		bar.setProgress(1);
		return bar;
	}
	
	public static void setBarProgress(String questName, Object obj, double amount, double size) {
		if (!checkBar(obj)) return;
		BossBar bar = (BossBar) obj;
		bar.setProgress((double) (size - amount) / size);
		bar.setTitle(Lang.MobsProgression.format(questName, (int) (size - amount), (int) size));
	}
	
	public static void showBar(Object bar, Player p) {
		if (!checkBar(bar)) return;
		((BossBar) bar).addPlayer(p);
	}
	
	public static void hideBar(Object bar, Player p) {
		if (!checkBar(bar)) return;
		((BossBar) bar).removePlayer(p);
	}
	
	public static void removeBar(Object bar) {
		if (!checkBar(bar)) return;
		((BossBar) bar).removeAll();
	}
	
	private static boolean checkBar(Object bar){
		if (bar == null) return false;
		if (!(bar instanceof BossBar)) throw new IllegalArgumentException("Error in post 1.9 compatibility : object is not a BossBar.");
		return true;
	}
	
}