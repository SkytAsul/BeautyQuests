package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.rewards.AbstractReward;
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
	public List<String> give(Player p) {
		Lang.QUEST_CHECKPOINT.send(p);
		return null;
	}
	
	public void applies(Player p) {
		Utils.giveRewards(p, actions);
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
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, actions).create(event.getPlayer());
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("actions", Utils.serializeList(actions, AbstractReward::serialize));
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		actions = QuestObject.deserializeList((List<Map<?, ?>>) savedDatas.get("actions"), AbstractReward::deserialize);
	}
	
}
