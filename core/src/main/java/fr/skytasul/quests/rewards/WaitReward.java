package fr.skytasul.quests.rewards;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;

public class WaitReward extends AbstractReward {
	
	private int delay;
	
	public WaitReward() {
		this(0);
	}
	
	public WaitReward(int delay) {
		this.delay = delay;
	}
	
	@Override
	public boolean isAsync() {
		return delay != 0;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(Lang.Ticks.format(delay)), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.REWARD_EDITOR_WAIT.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), () -> {
			if (delay == 0) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			delay = obj;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
	}
	
	@Override
	public List<String> give(Player p) {
		try {
			Thread.sleep(delay * 50L);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return new WaitReward(delay);
	}
	
	@Override
	protected void save(ConfigurationSection section) {
		section.set("delay", delay);
	}
	
	@Override
	protected void load(ConfigurationSection section) {
		delay = section.getInt("delay");
	}
	
}
