package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.items.ItemsGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemReward extends AbstractReward {

	public List<ItemStack> items;

	public ItemReward(){
		this(null, new ArrayList<>());
	}

	public ItemReward(String customDescription, List<ItemStack> items) {
		super(customDescription);
		this.items = items;
	}

	@Override
	public List<String> give(Player p) {
		Utils.giveItems(p, items);
		if (items.isEmpty())
			return Collections.emptyList();
		return Arrays.asList(getItemsSizeString());
	}

	@Override
	public AbstractReward clone() {
		return new ItemReward(getCustomDescription(), items);
	}

	@Override
	public String getDefaultDescription(Player p) {
		return Lang.AmountItems.toString();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("items_amount", () -> Integer.toString(getItemsSize()));
	}

	private int getItemsSize() {
		return items.stream().mapToInt(ItemStack::getAmount).sum();
	}

	private String getItemsSizeString() {
		return Lang.AmountItems.quickFormat("items_amount", getItemsSize());
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(getItemsSizeString());
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ItemsGUI(items -> {
			this.items = items;
			event.reopenGUI();
		}, items).open(event.getPlayer());
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("items", Utils.serializeList(items, ItemStack::serialize));
	}

	@Override
	public void load(ConfigurationSection section){
		super.load(section);
		items.addAll(Utils.deserializeList(section.getMapList("items"), ItemStack::deserialize));
	}

}
