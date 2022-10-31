package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;

public class RandomReward extends AbstractReward {
	
	private List<AbstractReward> rewards;
	private int min, max;
	
	public RandomReward() {
		this(new ArrayList<>(), 1, 1);
	}
	
	public RandomReward(List<AbstractReward> rewards, int min, int max) {
		this.rewards = rewards;
		this.min = min;
		this.max = max;
	}
	
	public void setMinMax(int min, int max) {
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		
		if (max > rewards.size())
			BeautyQuests.logger.warning("Random reward with max amount (" + max + ") greater than amount of rewards available (" + rewards.size() + ") in " + debugName());
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
				BeautyQuests.logger.severe("Error when giving random reward " + reward.getName() + " to " + p.getName(), ex);
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
		return new RandomReward(new ArrayList<>(rewards), min, max);
	}
	
	@Override
	public String getDescription(Player p) {
		return rewards
				.stream()
				.map(req -> req.getDescription(p))
				.filter(Objects::nonNull)
				.collect(Collectors.joining("{JOIN}"));
	}
	
	@Override
	public String[] getLore() {
		return new String[] {
				QuestOption.formatDescription(Lang.actions.format(rewards.size())),
				"§8 | min: §7" + min + "§8 | max: §7" + max,
				"",
				"§7" + Lang.ClickLeft + " > §7" + Lang.rewardRandomRewards,
				"§7" + Lang.ClickRight + " > §7" + Lang.rewardRandomMinMax,
				"§7" + Lang.ClickMiddle + " > §c" + Lang.Remove };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation() || event.getClick().isLeftClick()) {
			QuestsAPI.getRewards().createGUI(QuestObjectLocation.OTHER, rewards -> {
				this.rewards = rewards;
				event.reopenGUI();
			}, rewards).create(event.getPlayer());
		}else if (event.getClick().isRightClick()) {
			Lang.REWARD_EDITOR_RANDOM_MIN.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopenGUI, min -> {
				Lang.REWARD_EDITOR_RANDOM_MAX.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopenGUI, max -> {
					setMinMax(min, max == null ? min : max);
					event.reopenGUI();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).passNullIntoEndConsumer().enter();
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
		}
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("rewards", SerializableObject.serializeList(rewards));
		section.set("min", min);
		section.set("max", max);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		rewards = SerializableObject.deserializeList(section.getMapList("rewards"), AbstractReward::deserialize);
		setMinMax(section.getInt("min"), section.getInt("max"));
	}
	
}
