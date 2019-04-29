package fr.skytasul.quests.stages;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class StageBringBack extends StageNPC{
	
	private ItemStack[] items;
	private String splitted;
	private String line;
	
	public StageBringBack(StageManager manager, NPC npc, ItemStack[] items) {
		super(manager, npc);
		this.bringBack = this;
		this.items = items;

		String[] array = new String[items.length]; // create messages on beginning
		for (int i = 0; i < array.length; i++){
			array[i] = QuestsConfiguration.getItemNameColor() + Utils.getStringFromItemStack(items[i], QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.showDescriptionItemsXOne(Source.FORCESPLIT));
		}
		splitted = Utils.descriptionLines(Source.FORCESPLIT, array);
		if (QuestsConfiguration.showDescriptionItemsXOne(Source.FORCESPLIT)){
			for (int i = 0; i < array.length; i++){
				array[i] = QuestsConfiguration.getItemNameColor() + Utils.getStringFromItemStack(items[i], QuestsConfiguration.getItemAmountColor(), false);
			}
		}
		line = Utils.descriptionLines(Source.FORCELINE, array);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		if (super.dialogs.containsKey(e.getPlayer())){ // if the player has started the dialog (so items has been removed from his inventory)
			e.getPlayer().getInventory().addItem(items);
		}
	}
	
	public boolean checkItems(Player p, boolean msg){
		boolean done = true;
		for (ItemStack is : items){
			if (!Utils.containsItems(p.getInventory(), is, is.getAmount())){
				done = false;
				break;
			}
		}
		if (done) return true;
		if (msg) Utils.sendNPCMessage(p, Lang.NEED_OBJECTS.toString(), npc, 1, 1, line);
		return false;
	}
	
	public void removeItems(Player p){
		for(ItemStack is : items){
			Utils.removeItems(p.getInventory(), is.clone());
		}
		p.updateInventory();
	}
	
	public ItemStack[] getItems(){
		return items;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_ITEMS.toString() + " " + (QuestsConfiguration.splitDescription(source) ? splitted : line), npcName());
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{QuestsConfiguration.splitDescription(source) ? splitted : line, npcName()};
	}

	public void launch(Player p){
		super.launch(p);
		if (sendStartMessage()) Utils.sendNPCMessage(p, Lang.NEED_OBJECTS.toString(), npc, 1, 1, line);
	}
	
	
	public void serialize(Map<String, Object> map) {
		super.serialize(map);
		map.put("items", items);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageBringBack st = new StageBringBack(manager,
				CitizensAPI.getNPCRegistry().getById((int) map.get("npcID")),
				((List<ItemStack>) map.get("items")).toArray(new ItemStack[0]));
		st.loadDatas(map);
		return st;
	}

}
