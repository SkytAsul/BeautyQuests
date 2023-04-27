package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.serializable.SerializableObject;

public class RandomReward extends AbstractReward {
	
	private List<AbstractReward> rewards;
	private int min, max;
	
	public RandomReward() {
		this(null, new ArrayList<>(), 1, 1);
	}
	
	public RandomReward(String customDescription, List<AbstractReward> rewards, int min, int max) {
		super(customDescription);
		this.rewards = rewards;
		this.min = min;
		this.max = max;
	}
	
	public void setMinMax(int min, int max) {
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		
		if (max > rewards.size())
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Random reward with max amount (" + max + ") greater than amount of rewards available (" + rewards.size() + ") in " + debugName());
	}
	
	@Override
	public List<String> give(Player p) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int amount = min == max ? min : random.nextInt(min, max + 1);
		
		List<AbstractReward> left = new ArrayList<>(rewards);
		List<String> msg = new ArrayList<>();
		
		for (int i = 0; i < amount; i++) {
			if (left.isEmpty()) break;
			AbstractReward reward = left.remove(random.nextInt(left.size()));
			try {
				List<String> messages = reward.give(p);
				if (messages != null) msg.addAll(messages);
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error when giving random reward " + reward.getName() + " to " + p.getName(), ex);
			}
		}
		
		return msg;
	}
	
	@Override
	public boolean isAsync() {
		return rewards.stream().anyMatch(AbstractReward::isAsync);
	}
	
	@Override
	public AbstractReward clone() {
		return new RandomReward(getCustomDescription(), new ArrayList<>(rewards), min, max);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return rewards
				.stream()
				.map(req -> req.getDescription(p))
				.filter(Objects::nonNull)
				.collect(Collectors.joining("{JOIN}"));
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.actions.format(rewards.size()));
		loreBuilder.addDescriptionRaw("§8 | min: §7" + min + "§8 | max: §7" + max);
		loreBuilder.addClick(ClickType.LEFT, Lang.rewardRandomRewards.toString());
		loreBuilder.addClick(ClickType.RIGHT, Lang.rewardRandomMinMax.toString());
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation() || event.getClick().isLeftClick()) {
			QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.OTHER, rewards -> {
				this.rewards = rewards;
				event.reopenGUI();
			}, rewards).open(event.getPlayer());
		}else if (event.getClick().isRightClick()) {
			Lang.REWARD_EDITOR_RANDOM_MIN.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopenGUI, min -> {
				Lang.REWARD_EDITOR_RANDOM_MAX.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopenGUI, max -> {
					setMinMax(min, max == null ? min : max);
					event.reopenGUI();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).passNullIntoEndConsumer().start();
			}, NumberParser.INTEGER_PARSER_POSITIVE).start();
		}
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("rewards", SerializableObject.serializeList(rewards));
		section.set("min", min);
		section.set("max", max);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		rewards = SerializableObject.deserializeList(section.getMapList("rewards"), AbstractReward::deserialize);
		setMinMax(section.getInt("min"), section.getInt("max"));
	}
	
}
