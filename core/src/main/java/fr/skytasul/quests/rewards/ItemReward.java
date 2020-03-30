package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class ItemReward extends AbstractReward {

	public final List<ItemStack> items = new ArrayList<>();
	
	public ItemReward(){
		super("itemReward");
	}
	
	public ItemReward(List<ItemStack> items){
		this();
		this.items.addAll(items);
	}

	public String give(Player p){
		int amount = 0;
		for (ItemStack is : items){
			Utils.giveItem(p, is);
			amount += is.getAmount();
		}
		if (amount == 0) return null;
		return amount + " " + Lang.Item.toString();
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("items", Utils.serializeList(items, ItemStack::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		items.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("items"), ItemStack::deserialize));
	}

	public static class Creator implements RewardCreationRunnables {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			if (!datas.containsKey("items")) datas.put("items", new ArrayList<>());
			Inventories.create(p, new ItemsGUI(() -> {
				gui.reopen(p, true);
			}, (List<ItemStack>) datas.get("items")));
		}

		public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is) {
			datas.put("items", ((ItemReward) reward).items);
		}

		public AbstractReward finish(Map<String, Object> datas) {
			return new ItemReward((List<ItemStack>) datas.get("items"));
		}

	}

}
