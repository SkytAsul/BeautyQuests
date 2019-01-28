package fr.skytasul.quests.stages;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class StageBringBack extends StageNPC{
	
	private ItemStack[] items;
	
	public StageBringBack(StageManager manager, NPC npc, ItemStack[] items) {
		super(manager, npc);
		this.bringBack = this;
		this.items = items;
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
		if (msg) Utils.sendNPCMessage(p, Lang.NEED_OBJECTS.toString(), npc, 1, 1, Utils.itemsToFormattedString(Utils.getStringArrayFromItemStackArray(items, QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.getItemNameColor())));
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

	public String descriptionLine(PlayerAccount acc){
		return Utils.format(Lang.SCOREBOARD_ITEMS.toString() + " " + Utils.itemsToFormattedString(Utils.getStringArrayFromItemStackArray(items, QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.getItemNameColor())), (npc != null) ? npc.getName() : "§c§lerror");
	}
	
	protected String descriptionMenu(PlayerAccount acc){
		return Utils.format(Lang.SCOREBOARD_ITEMS.toString() + "\\n" + Utils.itemsToString(Utils.getStringArrayFromItemStackArray(items, QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.getItemNameColor()), "\\n"), (npc != null) ? npc.getName() : "§c§lerror");
	}

	public void launch(Player p){
		super.launch(p);
		if (sendStartMessage()) Utils.sendNPCMessage(p, Lang.NEED_OBJECTS.toString(), npc, 1, 1, Utils.itemsToFormattedString(Utils.getStringArrayFromItemStackArray(items, QuestsConfiguration.getItemAmountColor(), QuestsConfiguration.getItemNameColor())));
	}
	
	
	public Map<String, Object> serialize(Map<String, Object> map) {
		map = super.serialize(map);
		map.put("items", items);
		return map;
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageBringBack st = new StageBringBack(manager,
				CitizensAPI.getNPCRegistry().getById((int) map.get("npcID")),
				((List<ItemStack>) map.get("items")).toArray(new ItemStack[0]));
		st.loadDatas(map);
		return st;
	}

}
