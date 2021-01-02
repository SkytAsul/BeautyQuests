package fr.skytasul.quests.utils.compatibility.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import fr.skytasul.quests.utils.Lang;

public class MobBossBar extends BQBossBarImplementation {
	
	private String questName;
	private int mobsAmount;
	
	public MobBossBar(String questName, int mobsAmount) {
		super(Lang.MobsProgression.format(questName, 100, 100), BarColor.YELLOW, BarStyle.SOLID);
		this.questName = questName;
		this.mobsAmount = mobsAmount;
		
		BarStyle style = null;
		if (mobsAmount % 20 == 0) {
			style = BarStyle.SEGMENTED_20;
		}else if (mobsAmount % 10 == 0) {
			style = BarStyle.SEGMENTED_10;
		}else if (mobsAmount % 12 == 0) {
			style = BarStyle.SEGMENTED_12;
		}else if (mobsAmount % 6 == 0) {
			style = BarStyle.SEGMENTED_6;
		}
		if (style != null) super.bar.setStyle(style);
		
		bar.setProgress(1);
	}
	
	public void setProgress(int amount) {
		setProgress((double) (mobsAmount - amount) / (double) mobsAmount);
		setTitle(Lang.MobsProgression.format(questName, mobsAmount - amount, mobsAmount));
	}
	
}
