package fr.skytasul.quests.rewards;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
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
				event.cancel();
				return;
			}
			
			title = newTitle;
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
	public void save(ConfigurationSection section) {
		if (title != null) title.serialize(section.createSection("title"));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		title = section.contains("title") ? Title.deserialize(section.getConfigurationSection("title")) : null;
	}
	
}
