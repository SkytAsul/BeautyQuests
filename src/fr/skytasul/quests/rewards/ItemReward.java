package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
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
		List<Map<String, Object>> ls = new ArrayList<>();
		for (ItemStack is : items){
			ls.add(is.serialize());
		}
		datas.put("items", ls);
	}

	protected void load(Map<String, Object> savedDatas){
		List<Map<String, Object>> ls = (List<Map<String, Object>>) savedDatas.get("items");
		for (Map<String, Object> m : ls){
			items.add(ItemStack.deserialize(m));
		}
	}

}
