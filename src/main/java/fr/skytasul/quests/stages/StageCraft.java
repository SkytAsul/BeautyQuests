package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class StageCraft extends AbstractStage {

	private ItemStack result;
	private Map<PlayerAccount, Integer> playerAmounts = new HashMap<>();
	
	public StageCraft(StageManager manager, ItemStack result){
		super(manager);
		this.result = result;
	}
	
	public ItemStack getItem(){
		return result;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onCraft(CraftItemEvent e){
		Player p = (Player) e.getView().getPlayer();
		ItemStack clicked = e.getView().getTopInventory().getItem(0);
		System.out.println(clicked.toString());
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (manager.hasStageLaunched(acc, this)){
			if (clicked.isSimilar(result)){
				int newAmount = playerAmounts.get(acc) - clicked.getAmount();
				if (newAmount <= 0){
					playerAmounts.remove(acc);
					finishStage(p);
				}else {
					playerAmounts.put(acc, newAmount);
				}
			}
		}
	}
	
	public void start(PlayerAccount account){
		super.start(account);
		playerAmounts.put(account, result.getAmount());
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_CRAFT.format(Utils.getStringFromNameAndAmount(ItemUtils.getName(result, true), QuestsConfiguration.getItemAmountColor(), playerAmounts.get(acc), false));
	}

	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new Object[]{Utils.getStringFromNameAndAmount(ItemUtils.getName(result, true), QuestsConfiguration.getItemAmountColor(), playerAmounts.get(acc), false)};
	}
	
	protected void serialize(Map<String, Object> map){
		map.put("result", result.serialize());
		Map<String, Integer> playerSerialized = new HashMap<>();
		playerAmounts.forEach((acc, amount) -> playerSerialized.put(acc.getIndex(), amount));
		map.put("players", playerSerialized);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageCraft stage = new StageCraft(manager, ItemStack.deserialize((Map<String, Object>) map.get("result")));
		((Map<String, Object>) map.get("players")).forEach((acc, amount) -> stage.playerAmounts.put(PlayersManager.getByIndex(acc), (int) amount));
		return stage;
	}

}
