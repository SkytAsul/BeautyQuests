package fr.skytasul.quests.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.misc.TitleGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.Title;

public class TitleReward extends AbstractReward {
	
	private Title title;
	
	public TitleReward() {
		this(null);
	}
	
	public TitleReward(Title title) {
		super("titleReward");
		this.title = title;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { title == null ? Lang.NotSet.toString() : Lang.optionValue.format(title.toString()), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new TitleGUI(newTitle -> {
			if (newTitle == null) {
				if (title == null) gui.remove(this);
			}else title = newTitle;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}).edit(title).create(p);
	}
	
	@Override
	public List<String> give(Player p) {
		if (title != null) title.send(p);
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return new TitleReward(title);
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		if (title != null) datas.put("title", title.serialize());
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		title = savedDatas.containsKey("title") ? Title.deserialize((Map<String, Object>) savedDatas.get("title")) : null;
	}
	
}
