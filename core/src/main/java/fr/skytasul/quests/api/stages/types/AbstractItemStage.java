package fr.skytasul.quests.api.stages.types;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.misc.ItemComparisonGUI;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public abstract class AbstractItemStage extends AbstractCountableStage<ItemStack> {
	
	protected final ItemComparisonMap comparisons;

	protected AbstractItemStage(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> objects, ItemComparisonMap comparisons) {
		super(branch, objects);
		this.comparisons = comparisons;
	}
	
	protected AbstractItemStage(QuestBranch branch, ConfigurationSection section) {
		super(branch, new HashMap<>());
		
		if (section.contains("itemComparisons")) {
			comparisons = new ItemComparisonMap(section.getConfigurationSection("itemComparisons"));
		}else comparisons = new ItemComparisonMap();
		
		super.deserialize(section);
	}

	@Override
	protected ItemStack cloneObject(ItemStack object) {
		return object.clone();
	}

	@Override
	protected boolean objectApplies(ItemStack object, Object other) {
		return comparisons.isSimilar(object, (ItemStack) other);
	}

	@Override
	protected String getName(ItemStack object) {
		return ItemUtils.getName(object, true);
	}

	@Override
	protected Object serialize(ItemStack object) {
		return object.serialize();
	}

	@Override
	protected ItemStack deserialize(Object object) {
		return ItemStack.deserialize((Map<String, Object>) object);
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		super.serialize(section);
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("itemComparisons", comparisons.getNotDefault());
	}
	
	public abstract static class Creator<T extends AbstractItemStage> extends StageCreation<T> {
		
		private static final ItemStack stageComparison = ItemUtils.item(XMaterial.PRISMARINE_SHARD, Lang.stageItemsComparison.toString());
		
		private List<ItemStack> items;
		private ItemComparisonMap comparisons = new ItemComparisonMap();
		
		protected Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, getEditItem().clone(), (p, item) -> {
				new ItemsGUI(items -> {
					setItems(items);
					reopenGUI(p, true);
				}, items).create(p);
			});
			line.setItem(7, stageComparison.clone(), (p, item) -> {
				new ItemComparisonGUI(comparisons, () -> {
					setComparisons(comparisons);
					reopenGUI(p, true);
				}).create(p);
			});
		}
		
		protected abstract ItemStack getEditItem();
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			line.editItem(6, ItemUtils.lore(line.getItem(6), Lang.optionValue.format(Lang.AmountItems.format(this.items.size()))));
		}
		
		public void setComparisons(ItemComparisonMap comparisons) {
			this.comparisons = comparisons;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(Lang.AmountComparisons.format(this.comparisons.getEffective().size()))));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new ItemsGUI(items -> {
				setItems(items);
				reopenGUI(p, true);
			}, Collections.emptyList()).create(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setItems(stage.getObjects().values().stream().map(entry -> {
				ItemStack item = entry.getKey().clone();
				item.setAmount(entry.getValue());
				return item;
			}).collect(Collectors.toList()));
			setComparisons(stage.comparisons.clone());
		}
		
		@Override
		public final T finishStage(QuestBranch branch) {
			Map<Integer, Entry<ItemStack, Integer>> itemsMap = new HashMap<>();
			for (int i = 0; i < items.size(); i++) {
				ItemStack item = items.get(i);
				int amount = item.getAmount();
				item.setAmount(1);
				itemsMap.put(i, new AbstractMap.SimpleEntry<>(item, amount));
			}
			return finishStage(branch, itemsMap, comparisons);
		}
		
		protected abstract T finishStage(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> itemsMap, ItemComparisonMap comparisons);
		
	}
	
}