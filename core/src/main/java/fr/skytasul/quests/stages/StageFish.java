package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageFish extends AbstractCountableStage<ItemStack> {
	
	public StageFish(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> fishes) {
		super(branch, fishes);
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

	protected boolean objectApplies(ItemStack object, Object other) {
		return object.isSimilar((ItemStack) other);
	}

	protected String getName(ItemStack object) {
		return ItemUtils.getName(object, true);
	}

	protected Object serialize(ItemStack object) {
		return object.serialize();
	}

	protected ItemStack deserialize(Object object) {
		return ItemStack.deserialize((Map<String, Object>) object);
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_FISH.format(super.descriptionLine(acc, source));
	}
	
	public static StageFish deserialize(Map<String, Object> map, QuestBranch branch) {
		Map<Integer, Entry<ItemStack, Integer>> objects = new HashMap<>();

		if (map.containsKey("items")) {
			List<ItemStack> list = (List<ItemStack>) map.get("items");
			for (int i = 0; i < list.size(); i++) {
				ItemStack is = list.get(i);
				is.setAmount(1);
				objects.put(i, new AbstractMap.SimpleEntry<>(is, is.getAmount()));
			}
		}

		StageFish stage = new StageFish(branch, objects);
		stage.deserialize(map);

		return stage;
	}

	public static class Creator extends StageCreation<StageFish> {
		
		private List<ItemStack> items;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString()), (p, item) -> {
				new ItemsGUI(items -> {
					setItems(items);
					reopenGUI(p, true);
				}, items).create(p);
			});
		}
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			line.editItem(6, ItemUtils.lore(line.getItem(6), Lang.optionValue.format(this.items.size() + " fish(es)")));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new ItemsGUI(items -> {
				setItems(items);
				reopenGUI(p, true);
			}, Collections.EMPTY_LIST).create(p);
		}

		@Override
		public void edit(StageFish stage) {
			super.edit(stage);
			setItems(stage.getObjects().values().stream().map(entry -> {
				ItemStack item = entry.getKey().clone();
				item.setAmount(entry.getValue());
				return item;
			}).collect(Collectors.toList()));
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
			return new StageFish(branch, itemsMap);
		}
	}

}
