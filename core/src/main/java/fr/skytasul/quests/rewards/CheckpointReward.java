package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class CheckpointReward extends AbstractReward {
	
	private List<AbstractReward> actions;
	
	public CheckpointReward() {
		this(new ArrayList<>());
	}
	
	public CheckpointReward(List<AbstractReward> actions) {
		super("checkpointReward");
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
		return new String[] { QuestOption.formatDescription(Lang.actions.format(actions.size())), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new QuestObjectGUI<>(Lang.INVENTORY_CHECKPOINT_ACTIONS.toString(), QuestObjectLocation.CHECKPOINT, QuestsAPI.rewards.values(), rewards -> {
			actions = rewards;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, actions).create(p);
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("actions", Utils.serializeList(actions, AbstractReward::serialize));
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		actions = Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("actions"), map -> {
			try {
				return super.deserialize(map);
			}catch (ClassNotFoundException e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a quest object (class " + map.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
			}
			return null;
		});
	}
	
}
