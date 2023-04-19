package fr.skytasul.quests.rewards;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.misc.TitleGUI;
import fr.skytasul.quests.utils.types.Title;

public class TitleReward extends AbstractReward {
	
	private Title title;
	
	public TitleReward() {}
	
	public TitleReward(String customDescription, Title title) {
		super(customDescription);
		this.title = title;
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(title);
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
		return new TitleReward(getCustomDescription(), title);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		if (title != null) title.serialize(section.createSection("title"));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		title = section.contains("title") ? Title.deserialize(section.getConfigurationSection("title")) : null;
	}
	
}
