package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.creation.ItemsGUI;

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
		int amount = items.stream().mapToInt(ItemStack::getAmount).sum();
		return amount == 0 ? null : Arrays.asList(amount + " " + Lang.Item.toString());
	}

	@Override
	public AbstractReward clone() {
		return new ItemReward(getCustomDescription(), items);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return items.stream().mapToInt(ItemStack::getAmount).sum() + " " + Lang.Item.toString();
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(items.size() + " " + Lang.Item.toString());
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
