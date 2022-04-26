package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.misc.ItemComparisonGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageFish extends AbstractCountableStage<ItemStack> {
	
	private final ItemComparisonMap comparisons;
	
	public StageFish(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> fishes, ItemComparisonMap comparisons) {
		super(branch, fishes);
		this.comparisons = comparisons;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFish(PlayerFishEvent e){
		if (e.getState() == State.CAUGHT_FISH && e.getCaught() instanceof Item){
			Player p = e.getPlayer();
			PlayerAccount acc = PlayersManager.getPlayerAccount(p);
			Item item = (Item) e.getCaught();
			if (item.isDead() || !branch.hasStageLaunched(acc, this)) return;
			ItemStack fish = item.getItemStack();
			event(acc, p, fish, fish.getAmount());
		}
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
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_FISH.format(super.descriptionLine(acc, source));
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		super.serialize(section);
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("itemComparisons", comparisons.getNotDefault());
	}
	
	public static StageFish deserialize(ConfigurationSection section, QuestBranch branch) {
		ItemComparisonMap comparisons;
		if (section.contains("itemComparisons")) {
			comparisons = new ItemComparisonMap((Map) section.getConfigurationSection("itemComparisons").getValues(false));
		}else comparisons = new ItemComparisonMap();

		StageFish stage = new StageFish(branch, new HashMap<>(), comparisons);
		stage.deserialize(section);

		return stage;
	}

	public static class Creator extends StageCreation<StageFish> {
		
		private static final ItemStack stageComparison = ItemUtils.item(XMaterial.PRISMARINE_SHARD, Lang.stageItemsComparison.toString());
		
		private List<ItemStack> items;
		private ItemComparisonMap comparisons = new ItemComparisonMap();
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString()), (p, item) -> {
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
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			line.editItem(6, ItemUtils.lore(line.getItem(6), Lang.optionValue.format(this.items.size() + " fish(es)")));
		}
		
		public void setComparisons(ItemComparisonMap comparisons) {
			this.comparisons = comparisons;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(this.comparisons.getEffective().size() + " comparison(s)")));
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
		public void edit(StageFish stage) {
			super.edit(stage);
			setItems(stage.getObjects().values().stream().map(entry -> {
				ItemStack item = entry.getKey().clone();
				item.setAmount(entry.getValue());
				return item;
			}).collect(Collectors.toList()));
			setComparisons(stage.comparisons.clone());
		}
		
		@Override
		public StageFish finishStage(QuestBranch branch) {
			Map<Integer, Entry<ItemStack, Integer>> itemsMap = new HashMap<>();
			for (int i = 0; i < items.size(); i++) {
				ItemStack item = items.get(i);
				int amount = item.getAmount();
				item.setAmount(1);
				itemsMap.put(i, new AbstractMap.SimpleEntry<>(item, amount));
			}
			return new StageFish(branch, itemsMap, comparisons);
		}
	}

}
