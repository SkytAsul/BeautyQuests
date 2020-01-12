package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class StageBringBack extends StageNPC{
	
	private final ItemStack[] items;
	private Map<ItemStack, Integer> amountsMap = new HashMap<>();
	private String splitted;
	private String line;
	
	public StageBringBack(QuestBranch branch, NPC npc, ItemStack[] items) {
		super(branch, npc);
		this.bringBack = this;
		this.items = items;
		for (ItemStack item : items) {
			int amount = (amountsMap.containsKey(item) ? amountsMap.get(item) : 0) + item.getAmount();
			amountsMap.put(item, amount);
		}

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
	
	public boolean checkItems(Player p, boolean msg){
		boolean done = true;
		for (Entry<ItemStack, Integer> en : amountsMap.entrySet()) {
			if (!Utils.containsItems(p.getInventory(), en.getKey(), en.getValue())) {
				done = false;
				break;
			}
		}
		if (done) return true;
		if (msg) Lang.NpcText.sendWP(p, npc.getName(), Lang.NEED_OBJECTS.format(line), 1, 1);
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

	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent() && sendStartMessage()) Lang.NpcText.sendWP(acc.getPlayer(), npc.getName(), Lang.NEED_OBJECTS.format(line), 1, 1);
	}
	
	
	public void serialize(Map<String, Object> map) {
		super.serialize(map);
		map.put("items", items);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageBringBack st = new StageBringBack(branch,
				CitizensAPI.getNPCRegistry().getById((int) map.get("npcID")),
				((List<ItemStack>) map.get("items")).toArray(new ItemStack[0]));
		st.loadDatas(map);
		return st;
	}

}
