package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class StageFish extends AbstractStage{

	private ItemStack[] fishes;
	private Map<PlayerAccount, List<ItemStack>> players = new HashMap<>();
	
	public StageFish(StageManager manager, ItemStack[] fishes){
		super(manager);
		this.fishes = fishes;
	}

	@EventHandler
	public void onFish(PlayerFishEvent e){
		if (e.getState() == State.CAUGHT_FISH && e.getCaught() instanceof Item){
			if (!hasStarted(e.getPlayer())) return;
			ItemStack fish = ((Item) e.getCaught()).getItemStack();
			List<ItemStack> playerFishes = players.get(PlayersManager.getPlayerAccount(e.getPlayer()));
			for (ItemStack is : playerFishes){
				if (fish.isSimilar(is)){
					if (fish.getAmount() >= is.getAmount()){
						playerFishes.remove(is);
					}else is.setAmount(is.getAmount() - fish.getAmount());
					if (playerFishes.isEmpty()) finishStage(e.getPlayer());
					break;
				}
			}
		}
	}
	
	public ItemStack[] getFishes(){
		return fishes;
	}
	
	public void launch(Player p){
		super.launch(p);
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!players.containsKey(acc)){
			List<ItemStack> playerFishes = new ArrayList<>();
			for (ItemStack fish : fishes){
				playerFishes.add(fish.clone());
			}
			players.put(acc, playerFishes);
		}
	}
	
	public void end(PlayerAccount account) {
		super.end(account);
		players.remove(account);
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_FISH.format(Utils.descriptionLines(source, players.get(acc).stream().map(x -> QuestsConfiguration.getItemNameColor() + Utils.getStringFromItemStack(x, QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.showDescriptionItemsXOne(source))).collect(Collectors.toList()).toArray(new String[0])));
	}

	protected void serialize(Map<String, Object> map){
		map.put("items", fishes);
		Map<String, List<ItemStack>> re = new HashMap<>();
		for (Entry<PlayerAccount, List<ItemStack>> en : players.entrySet()){
			re.put(en.getKey().getIndex(), en.getValue());
		}
		map.put("remaining", re);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageFish stage = new StageFish(manager, ((List<ItemStack>) map.get("items")).toArray(new ItemStack[0]));

		Utils.deserializeAccountsMap(((Map<String, List<ItemStack>>) map.get("remaining")), stage.players, n -> n);
		
		return stage;
	}

}
