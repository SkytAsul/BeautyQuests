package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public abstract class QuestOptionRewards extends QuestOption<List<AbstractReward>> {
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		getValue().forEach(reward -> reward.attach(quest));
	}
	
	@Override
	public void detach() {
		super.detach();
		getValue().forEach(AbstractReward::detach);
	}
	
	@Override
	public Object save() {
		return Utils.serializeList(getValue(), AbstractReward::serialize);
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		List<Map<?, ?>> rlist = config.getMapList(key);
		for (Map<?, ?> rmap : rlist) {
			try {
				AbstractReward rew = AbstractReward.deserialize((Map<String, Object>) rmap, getAttachedQuest());
				getValue().add(rew);
				if (rew.isAsync()) addedAsyncReward();
			}catch (Exception e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a reward (class " + rmap.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public List<AbstractReward> cloneValue(List<AbstractReward> value) {
		return new ArrayList<>(value);
	}
	
	protected abstract void addedAsyncReward();
	
	private String[] getLore() {
		String count = "§7" + Lang.rewards.format(getValue().size());
		if (getItemDescription() == null) return new String[] { count };
		return new String[] { formatDescription(getItemDescription()), "", count };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		new RewardsGUI(rewards -> {
			setValue(rewards);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()).create(p);
	}
	
	public abstract XMaterial getItemMaterial();
	
	public abstract String getItemName();
	
	public String getItemDescription() {
		return null;
	}
	
}
