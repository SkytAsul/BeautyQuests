package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
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

	@Override
	public AbstractReward clone() {
		return new ItemReward(new ArrayList<>(items));
	}
	
	@Override
	public void itemClick(Player p, RewardsGUI gui, ItemStack clicked) {
		new ItemsGUI(() -> gui.reopen(p), items);
	}
	
	protected void save(Map<String, Object> datas){
		datas.put("items", Utils.serializeList(items, ItemStack::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		items.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("items"), ItemStack::deserialize));
	}

}
