package fr.skytasul.quests.stages;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.events.internal.BQCraftEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import fr.skytasul.quests.api.utils.progress.itemdescription.HasItemsDescriptionConfiguration.HasSingleObject;
import fr.skytasul.quests.gui.items.ItemComparisonGUI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

/**
 * @author SkytAsul, ezeiger92, TheBusyBiscuit
 */
public class StageCraft extends AbstractStage implements HasSingleObject, Listener {

	private final ItemStack result;
	private final ItemComparisonMap comparisons;

	public StageCraft(StageController controller, ItemStack result, ItemComparisonMap comparisons) {
		super(controller);
		this.result = result;
		this.comparisons = comparisons;
		if (result.getAmount() == 0) result.setAmount(1);
	}

	public ItemStack getItem(){
		return result;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFurnaceExtract(FurnaceExtractEvent event) {
		Player p = event.getPlayer();
		if (comparisons.isSimilar(result, new ItemStack(event.getItemType())) && hasStarted(p) && canUpdate(p, true)) {
			long amount = getPlayerAmount(PlayersManager.getPlayerAccount(p)) - event.getItemAmount();
			if (amount <= 0) {
				finishStage(p);
			}else {
				updateObjective(p, "amount", amount);
			}
		}
	}

	@EventHandler
	public void onCraft(BQCraftEvent event) {
		Player p = event.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			ItemStack item = event.getResult();
			if (comparisons.isSimilar(result, item)) {

				int recipeAmount = item.getAmount();

				switch (event.getClickEvent().getClick()) {
					case NUMBER_KEY:
						// If hotbar slot selected is full, crafting fails (vanilla behavior, even when items match)
						if (p.getInventory().getItem(event.getClickEvent().getHotbarButton()) != null) recipeAmount = 0;
						break;

					case DROP:
					case CONTROL_DROP:
						// If we are holding items, craft-via-drop fails (vanilla behavior)
						ItemStack cursor = event.getClickEvent().getCursor();
						if (cursor != null && cursor.getType() != Material.AIR) recipeAmount = 0;
						break;

					case SHIFT_RIGHT:
					case SHIFT_LEFT:
						if (recipeAmount == 0) break;

						int capacity = fits(item, p.getInventory());

						// If we can't fit everything, increase "space" to include the items dropped by crafting
						// (Think: Uncrafting 8 iron blocks into 1 slot)
						if (capacity < event.getMaxCraftable()) {
							recipeAmount = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;
						}else recipeAmount = event.getMaxCraftable();
						break;

					default:
						cursor = event.getClickEvent().getCursor();
						if (cursor != null && cursor.getType() != Material.AIR) {
							if (cursor.isSimilar(item)) {
								if (cursor.getAmount() + item.getAmount() > cursor.getMaxStackSize()) recipeAmount = 0;
							}else {
								recipeAmount = 0;
							}
						}
						break;
				}

				// No use continuing if we haven't actually crafted a thing
				if (recipeAmount == 0) return;

				long amount = getPlayerAmount(PlayersManager.getPlayerAccount(p)) - recipeAmount;
				if (amount <= 0) {
					finishStage(p);
				}else {
					updateObjective(p, "amount", amount);
				}
			}
		}
	}

	@Override
	public void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", result.getAmount());
	}

	@Override
	public long getPlayerAmount(PlayerAccount acc) {
		Long amount = getData(acc, "amount", Long.class);
		return amount == null ? 0 : amount.longValue();
	}

	@Override
	public @NotNull String getObjectName() {
		return ItemUtils.getName(result, true);
	}

	@Override
	public long getObjectAmount() {
		return result.getAmount();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		ProgressPlaceholders.registerObject(placeholders, "items", this);
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_CRAFT.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("result", result.serialize());

		if (!comparisons.getNotDefault().isEmpty()) section.createSection("itemComparisons", comparisons.getNotDefault());
	}

	public static StageCraft deserialize(ConfigurationSection section, StageController controller) {
		return new StageCraft(controller, ItemStack.deserialize(section.getConfigurationSection("result").getValues(false)), section.contains("itemComparisons") ? new ItemComparisonMap(section.getConfigurationSection("itemComparisons")) : new ItemComparisonMap());
	}

	public static int fits(ItemStack stack, Inventory inv) {
		int result = 0;

		for (ItemStack is : inv.getContents())
			if (is == null)
				result += stack.getMaxStackSize();
			else if (is.isSimilar(stack))
				result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);

		return result;
	}

	public static class Creator extends StageCreation<StageCraft> {

		private static final int ITEM_SLOT = 6, COMPARISONS_SLOT = 7;

		private ItemStack item;
		private ItemComparisonMap comparisons = new ItemComparisonMap();

		public Creator(@NotNull StageCreationContext<StageCraft> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(ITEM_SLOT, ItemUtils.item(XMaterial.CHEST, Lang.editItem.toString()), event -> {
				QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemSelection(is -> {
					if (is != null)
						setItem(is);
					event.reopen();
				}, true).open(event.getPlayer());
			});
			line.setItem(COMPARISONS_SLOT, ItemUtils.item(XMaterial.PRISMARINE_SHARD, Lang.stageItemsComparison.toString()), event -> {
				new ItemComparisonGUI(comparisons, () -> {
					setComparisons(comparisons);
					event.reopen();
				}).open(event.getPlayer());
			});
		}

		public void setItem(ItemStack item) {
			this.item = item;
			getLine().refreshItem(ITEM_SLOT,
					item2 -> ItemUtils.lore(item2,
							QuestOption.formatNullableValue(Utils.getStringFromItemStack(item, "§8", true))));
		}

		public void setComparisons(ItemComparisonMap comparisons) {
			this.comparisons = comparisons;
			getLine().refreshItem(COMPARISONS_SLOT, item -> ItemUtils.lore(item,
					QuestOption.formatNullableValue(
							Lang.AmountComparisons.quickFormat("amount", this.comparisons.getEffective().size()))));
		}

		@Override
		public void start(Player p) {
			super.start(p);
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemSelection(is -> {
				setItem(is);
				context.reopenGui();
			}, context::removeAndReopenGui).open(p);
		}

		@Override
		public void edit(StageCraft stage) {
			super.edit(stage);
			setItem(stage.getItem());
			setComparisons(stage.comparisons.clone());
		}

		@Override
		public StageCraft finishStage(StageController controller) {
			return new StageCraft(controller, item, comparisons);
		}
	}

}
