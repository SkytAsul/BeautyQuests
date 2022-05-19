package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class ItemReward extends AbstractReward {

	public List<ItemStack> items;
	
	public ItemReward(){
		this(new ArrayList<>());
	}
	
	public ItemReward(List<ItemStack> items){
		this.items = items;
	}

	@Override
	public List<String> give(Player p) {
		Utils.giveItems(p, items);
		int amount = items.stream().mapToInt(ItemStack::getAmount).sum();
		return amount == 0 ? null : Arrays.asList(amount + " " + Lang.Item.toString());
	}

	@Override
	public AbstractReward clone() {
		return new ItemReward(items);
	}
	
	@Override
	public String getDescription(Player p) {
		return items.stream().mapToInt(ItemStack::getAmount).sum() + " " + Lang.Item.toString();
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + items.size() + " " + Lang.Item.toString(), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ItemsGUI(items -> {
			this.items = items;
			event.updateItemLore(getLore());
			event.getGUI().reopen();
		}, items).create(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("items", Utils.serializeList(items, ItemStack::serialize));
	}

	@Override
	public void load(ConfigurationSection section){
		items.addAll(Utils.deserializeList(section.getMapList("items"), ItemStack::deserialize));
	}

}
