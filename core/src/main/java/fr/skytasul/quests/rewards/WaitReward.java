package fr.skytasul.quests.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;

public class WaitReward extends AbstractReward {
	
	private int delay;
	
	public WaitReward() {
		this(0);
	}
	
	public WaitReward(int delay) {
		super("wait");
		this.delay = delay;
	}
	
	@Override
	public boolean isAsync() {
		return delay != 0;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + Lang.Ticks.format(delay), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.REWARD_EDITOR_WAIT.send(p);
		new TextEditor<>(p, () -> {
			if (delay == 0) gui.remove(this);
			gui.reopen();
		}, obj -> {
			delay = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
	}
	
	@Override
	public List<String> give(Player p) {
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return new WaitReward(delay);
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("delay", delay);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		delay = (int) savedDatas.get("delay");
	}
	
}
