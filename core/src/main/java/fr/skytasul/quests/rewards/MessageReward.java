package fr.skytasul.quests.rewards;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class MessageReward extends AbstractReward {

	public String text;
	
	public MessageReward() {}
	
	public MessageReward(String customDescription, String text) {
		super(customDescription);
		this.text = text;
	}

	@Override
	public List<String> give(Player p) {
		Utils.sendOffMessage(p, text);
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new MessageReward(getCustomDescription(), text);
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(text);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.WRITE_MESSAGE.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::cancel, obj -> {
			this.text = obj;
			event.reopenGUI();
		}).enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("text", text);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		text = section.getString("text");
	}
	
}
