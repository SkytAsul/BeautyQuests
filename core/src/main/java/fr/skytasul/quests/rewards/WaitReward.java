package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardGiveContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class WaitReward extends AbstractReward {
	
	private int delay;
	
	public WaitReward() {}
	
	public WaitReward(String customDescription, int delay) {
		super(customDescription);
		this.delay = delay;
	}
	
	@Override
	public boolean isAsync() {
		return delay != 0;
	}
	
	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(getTicksString());
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("delay", this::getTicksString);
	}

	private @NotNull String getTicksString() {
		return Lang.Ticks.quickFormat("ticks", delay);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.REWARD_EDITOR_WAIT.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			delay = obj;
			event.reopenGUI();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
	}
	
	@Override
	public void give(RewardGiveContext context) {
		try {
			Thread.sleep(delay * 50L);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AbstractReward clone() {
		return new WaitReward(getCustomDescription(), delay);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("delay", delay);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		delay = section.getInt("delay");
	}
	
}
