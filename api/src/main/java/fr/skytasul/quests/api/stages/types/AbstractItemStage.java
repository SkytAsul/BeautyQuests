package fr.skytasul.quests.api.stages.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.Utils;

public abstract class AbstractItemStage extends AbstractCountableStage<ItemStack> {
	
	protected final @NotNull ItemComparisonMap comparisons;

	protected AbstractItemStage(@NotNull StageController controller,
			@NotNull List<@NotNull CountableObject<ItemStack>> objects,
			ItemComparisonMap comparisons) {
		super(controller, objects);
		this.comparisons = comparisons;
	}
	
	protected AbstractItemStage(@NotNull StageController controller, @NotNull ConfigurationSection section) {
		super(controller, new ArrayList<>());
		
		if (section.contains("itemComparisons")) {
			comparisons = new ItemComparisonMap(section.getConfigurationSection("itemComparisons"));
		}else comparisons = new ItemComparisonMap();
		
		super.deserialize(section);
	}

	@Override
	protected @NotNull String getPlaceholderKey() {
		return "items";
	}

	@Override
	protected @NotNull ItemStack cloneObject(@NotNull ItemStack object) {
		return object.clone();
	}

	@Override
	protected boolean objectApplies(@NotNull ItemStack object, @NotNull Object other) {
		return comparisons.isSimilar(object, (ItemStack) other);
	}

	@Override
	protected @NotNull String getName(@NotNull ItemStack object) {
		return ItemUtils.getName(object, true);
	}

	@Override
	protected @NotNull Object serialize(@NotNull ItemStack object) {
		return object.serialize();
	}

	@Override
	protected @NotNull ItemStack deserialize(@NotNull Object object) {
		return ItemStack.deserialize((Map<String, Object>) object);
	}

	@Override
	protected void serialize(@NotNull ConfigurationSection section) {
		super.serialize(section);
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("itemComparisons", comparisons.getNotDefault());
	}
	
	public abstract static class Creator<T extends AbstractItemStage> extends StageCreation<T> {
		
		private static final ItemStack stageComparison = ItemUtils.item(XMaterial.PRISMARINE_SHARD, Lang.stageItemsComparison.toString());
		
		private @NotNull List<ItemStack> items;
		private @NotNull ItemComparisonMap comparisons = new ItemComparisonMap();
		
		public Creator(@NotNull StageCreationContext<T> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(6, getEditItem().clone(), event -> {
				QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemsSelection(items -> {
					setItems(items);
					event.reopen();
				}, items).open(event.getPlayer());
			});
			line.setItem(7, stageComparison.clone(), event -> {
				QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemComparisonsSelection(comparisons, () -> {
					setComparisons(comparisons);
					event.reopen();
				}).open(event.getPlayer());
			});
		}
		
		protected abstract @NotNull ItemStack getEditItem();
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			getLine().refreshItemLoreOptionValue(6, Lang.AmountItems.quickFormat("amount", this.items.size()));
		}
		
		public void setComparisons(ItemComparisonMap comparisons) {
			this.comparisons = comparisons;
			getLine().refreshItemLoreOptionValue(7,
					Lang.AmountComparisons.quickFormat("amount", this.comparisons.getEffective().size()));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemsSelection(items -> {
				setItems(items);
				context.reopenGui();
			}, Collections.emptyList()).open(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setItems(stage.getObjects().stream().map(entry -> {
				ItemStack item = entry.getObject().clone();
				item.setAmount(entry.getAmount());
				return item;
			}).collect(Collectors.toList()));
			setComparisons(stage.comparisons.clone());
		}
		
		@Override
		public final T finishStage(StageController controller) {
			List<CountableObject<ItemStack>> itemsMap = new ArrayList<>();
			for (int i = 0; i < items.size(); i++) {
				ItemStack item = items.get(i);
				int amount = item.getAmount();
				item.setAmount(1);
				itemsMap.add(CountableObject.create(new UUID(item.hashCode(), 2478), item, amount));
			}
			return finishStage(controller, itemsMap, comparisons);
		}
		
		protected abstract T finishStage(@NotNull StageController controller,
				@NotNull List<@NotNull CountableObject<ItemStack>> items, @NotNull ItemComparisonMap comparisons);
		
	}

}