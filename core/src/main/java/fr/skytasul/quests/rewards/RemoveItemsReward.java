package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.items.ItemComparisonGUI;
import fr.skytasul.quests.gui.items.ItemsGUI;

public class RemoveItemsReward extends AbstractReward {

	private List<ItemStack> items;
	private ItemComparisonMap comparisons;
	
	public RemoveItemsReward(){
		this(null, new ArrayList<>(), new ItemComparisonMap());
	}
	
	public RemoveItemsReward(String customDescription, List<ItemStack> items, ItemComparisonMap comparisons) {
		super(customDescription);
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
		return new RemoveItemsReward(getCustomDescription(), items, comparisons);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return getItemsSizeString();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("items_amount", this::getItemsSizeString);
		placeholders.register("comparisons_amount", this::getComparisonsSizeString);
	}

	private String getItemsSizeString() {
		return Lang.AmountItems.quickFormat("amount", items.stream().mapToInt(ItemStack::getAmount).sum());
	}
	
	private @NotNull String getComparisonsSizeString() {
		return Lang.AmountComparisons.quickFormat("amount", comparisons.getEffective().size());
	}

	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(getItemsSizeString());
		loreBuilder.addDescription(getComparisonsSizeString());
		loreBuilder.addClick(ClickType.LEFT, Lang.stageItems.toString());
		loreBuilder.addClick(ClickType.RIGHT, Lang.stageItemsComparison.toString());
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation() || event.getClick().isLeftClick()) {
			new ItemsGUI(items -> {
				this.items = items;
				event.reopenGUI();
			}, items).open(event.getPlayer());
		}else if (event.getClick().isRightClick()) {
			new ItemComparisonGUI(comparisons, event::reopenGUI).open(event.getPlayer());
		}
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("items", Utils.serializeList(items, ItemStack::serialize));
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("comparisons", comparisons.getNotDefault());
	}

	@Override
	public void load(ConfigurationSection section){
		super.load(section);
		items.addAll(Utils.deserializeList(section.getMapList("items"), ItemStack::deserialize));
		if (section.contains("comparisons")) comparisons.setNotDefaultComparisons(section.getConfigurationSection("comparisons"));
	}

}
