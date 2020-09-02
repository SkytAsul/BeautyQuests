package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
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

	public static class Creator implements StageCreationRunnables<StageBringBack> {
		private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageItems.toString());

		public void start(Player p, LineData datas) {
			setItem(datas.getLine());
			List<ItemStack> items = new ArrayList<>();
			datas.put("items", items);
			new ItemsGUI(() -> {
				new SelectGUI(() -> {
					datas.getGUI().deleteStageLine(datas, p);
					datas.getGUI().reopen(p, true);
				}, npc -> {
					datas.getGUI().reopen(p, true);
					StageNPC.Creator.npcDone(npc, datas);
				}).create(p);
			}, items).create(p);
		}

		public static void setItem(Line line) {
			line.setItem(7, stageItems.clone(), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					Inventories.create(p, new ItemsGUI(() -> {
						datas.getGUI().reopen(p, true);
					}, (List<ItemStack>) datas.get("items")));
				}
			});
		}

		public StageBringBack finish(LineData datas, QuestBranch branch) {
			StageBringBack stage = new StageBringBack(branch, (NPC) datas.get("npc"), ((List<ItemStack>) datas.get("items")).toArray(new ItemStack[0]));
			StageNPC.Creator.setFinish(stage, datas);
			return stage;
		}

		public void edit(LineData datas, StageBringBack stage) {
			StageNPC.Creator.setEdit(stage, datas);
			datas.put("items", new ArrayList<>());
			((List<ItemStack>) datas.get("items")).addAll(Arrays.asList(stage.getItems()));
			setItem(datas.getLine());
		}
	}

}
