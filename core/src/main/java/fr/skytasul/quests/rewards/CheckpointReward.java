package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class CheckpointReward extends AbstractReward {
	
	private List<AbstractReward> actions;
	
	public CheckpointReward() {
		this(new ArrayList<>());
	}
	
	public CheckpointReward(List<AbstractReward> actions) {
		this.actions = actions;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		actions.forEach(rew -> rew.attach(quest));
	}
	
	@Override
	public void detach() {
		super.detach();
		actions.forEach(AbstractReward::detach);
	}
	
	@Override
	public List<String> give(Player p) {
		Lang.QUEST_CHECKPOINT.send(p);
		return null;
	}
	
	public void applies(Player p) {
		try {
			Utils.giveRewards(p, actions);
		} catch (InterruptingBranchException e) {
			BeautyQuests.logger.warning("Trying to interrupt branching in a checkpoint reward (useless). " + toString());
		}
	}
	
	@Override
	public AbstractReward clone() {
		return new CheckpointReward(new ArrayList<>(actions));
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(Lang.actions.format(actions.size())), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		QuestsAPI.getRewards().createGUI(Lang.INVENTORY_CHECKPOINT_ACTIONS.toString(), QuestObjectLocation.CHECKPOINT, rewards -> {
			actions = rewards;
			event.reopenGUI();
		}, actions, null).create(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("actions", SerializableObject.serializeList(actions));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		actions = SerializableObject.deserializeList(section.getMapList("actions"), AbstractReward::deserialize);
	}
	
}
