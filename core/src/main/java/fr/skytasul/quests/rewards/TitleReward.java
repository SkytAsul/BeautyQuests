package fr.skytasul.quests.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.misc.TitleGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.Title;

public class TitleReward extends AbstractReward {
	
	private Title title;
	
	public TitleReward() {}
	
	public TitleReward(Title title) {
		this.title = title;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { title == null ? Lang.NotSet.toString() : Lang.optionValue.format(title.toString()), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new TitleGUI(newTitle -> {
			if (newTitle == null) {
				if (title == null) event.getGUI().remove(this);
			}else title = newTitle;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}).edit(title).create(event.getPlayer());
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
