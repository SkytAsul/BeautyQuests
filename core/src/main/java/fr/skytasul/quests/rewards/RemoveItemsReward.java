package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.misc.ItemComparisonGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class RemoveItemsReward extends AbstractReward {

	private List<ItemStack> items;
	private ItemComparisonMap comparisons;
	
	public RemoveItemsReward(){
		this(new ArrayList<>(), new ItemComparisonMap());
	}
	
	public RemoveItemsReward(List<ItemStack> items, ItemComparisonMap comparisons) {
		this.items = items;
		this.comparisons = comparisons;
	}

	@Override
	public List<String> give(Player p) {
		int amount = 0;
		Inventory inventory = p.getInventory();
		for (ItemStack item : items) {
			comparisons.removeItems(inventory, item);
			amount += item.getAmount();
		}
		return amount == 0 ? null : Arrays.asList(Integer.toString(amount));
	}

	@Override
	public AbstractReward clone() {
		return new RemoveItemsReward(items, comparisons);
	}
	
	@Override
	public String getDescription(Player p) {
		return items.stream().mapToInt(ItemStack::getAmount).sum() + " " + Lang.Item.toString();
	}
	
	@Override
	public String[] getLore() {
		return new String[] {
				"§7" + items.size() + " " + Lang.Item.toString(),
				"§7" + comparisons.getEffective().size() + " comparison(s)",
				"",
				"§7" + Lang.ClickLeft.toString() + " > " + Lang.stageItems.toString(),
				"§7" + Lang.ClickRight.toString() + " > " + Lang.stageItemsComparison.toString(),
				"§7" + Lang.ClickMiddle.toString() + " > §c" + Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation() || event.getClick().isLeftClick()) {
			new ItemsGUI(items -> {
				this.items = items;
				event.updateItemLore(getLore());
				event.reopenGUI();
			}, items).create(event.getPlayer());
		}else if (event.getClick().isRightClick()) {
			new ItemComparisonGUI(comparisons, () -> {
				event.updateItemLore(getLore());
				event.reopenGUI();
			}).create(event.getPlayer());
		}
	}
	
	@Override
	protected void save(ConfigurationSection section) {
		section.set("items", Utils.serializeList(items, ItemStack::serialize));
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("comparisons", comparisons.getNotDefault());
	}

	@Override
	protected void load(ConfigurationSection section){
		items.addAll(Utils.deserializeList(section.getMapList("items"), ItemStack::deserialize));
		if (section.contains("comparisons")) comparisons.setNotDefaultComparisons(section.getConfigurationSection("comparisons"));
	}

}
