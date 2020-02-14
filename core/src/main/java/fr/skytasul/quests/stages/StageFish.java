package fr.skytasul.quests.stages;

import java.util.AbstractMap;
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
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;

public class StageFish extends AbstractCountableStage<ItemStack> {
	
	public StageFish(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> fishes) {
		super(branch, fishes);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFish(PlayerFishEvent e){
		if (e.getState() == State.CAUGHT_FISH && e.getCaught() instanceof Item){
			Player p = e.getPlayer();
			Item item = (Item) e.getCaught();
			if (item.isDead() || !hasStarted(p)) return;
			ItemStack fish = item.getItemStack();
			event(PlayersManager.getPlayerAccount(p), p, fish, fish.getAmount());
		}
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

}
