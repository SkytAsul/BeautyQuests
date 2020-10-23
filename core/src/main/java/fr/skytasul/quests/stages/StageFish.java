package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
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
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_FISH.format(super.descriptionLine(acc, source));
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
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

		if (map.containsKey("remaining")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			((Map<String, List<ItemStack>>) map.get("remaining")).forEach((acc, items) -> {
				Map<ItemStack, Integer> itemsMap = new HashMap<>();
				for (ItemStack item : items) {
					ItemStack itemOne = item.clone();
					itemOne.setAmount(1);
					itemsMap.put(itemOne, item.getAmount());
				}
				stage.migrateDatas(migration.getByIndex(acc), itemsMap);
			});
		}

		return stage;
	}

	public static class Creator implements StageCreationRunnables<StageFish> {
		public void start(Player p, LineData datas) {
			List<ItemStack> items = new ArrayList<>();
			datas.put("items", items);
			Inventories.create(p, new ItemsGUI(() -> {
				datas.getGUI().reopen(p, true);
				setItem(datas.getLine());
			}, items));
		}

		public StageFish finish(LineData datas, QuestBranch branch) {
			List<ItemStack> itemsList = datas.get("items");
			Map<Integer, Entry<ItemStack, Integer>> itemsMap = new HashMap<>();
			for (int i = 0; i < itemsList.size(); i++) {
				ItemStack item = itemsList.get(i);
				int amount = item.getAmount();
				item.setAmount(1);
				itemsMap.put(i, new AbstractMap.SimpleEntry<>(item, amount));
			}
			StageFish stage = new StageFish(branch, itemsMap);
			return stage;
		}

		public void edit(LineData datas, StageFish stage) {
			List<ItemStack> items = new ArrayList<>();
			Map<Integer, Entry<ItemStack, Integer>> itemsMap = stage.getObjects();
			for (Entry<ItemStack, Integer> itemEntry : itemsMap.values()) {
				ItemStack item = itemEntry.getKey().clone();
				item.setAmount(itemEntry.getValue());
				items.add(item);
			}
			datas.put("items", items);
			setItem(datas.getLine());
		}

		public static void setItem(Line line) {
			line.setItem(6, ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString()), new StageRunnable() {
				public void run(Player p, ItemStack item) {
					Inventories.create(p, new ItemsGUI(() -> {
						line.getGUI().reopen(p, true);
					}, line.get("items")));
				}
			});
		}
	}

}
