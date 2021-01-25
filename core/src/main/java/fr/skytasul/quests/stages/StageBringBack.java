package fr.skytasul.quests.stages;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageBringBack extends StageNPC{
	
	private final ItemStack[] items;
	private final String customMessage;
	
	private Map<ItemStack, Integer> amountsMap = new HashMap<>();
	private String splitted;
	private String line;
	
	public StageBringBack(QuestBranch branch, ItemStack[] items, String customMessage) {
		super(branch);
		this.customMessage = customMessage;
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
		if (msg) Lang.NpcText.sendWP(p, npcName(), Utils.format(getMessage(), line), 1, 1);
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
	
	private String getMessage() {
		return customMessage == null ? Lang.NEED_OBJECTS.toString() : customMessage;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_ITEMS.toString() + " " + (QuestsConfiguration.splitDescription(source) ? splitted : line), npcName());
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{QuestsConfiguration.splitDescription(source) ? splitted : line, npcName()};
	}

	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent() && sendStartMessage()) Lang.NpcText.sendWP(acc.getPlayer(), npcName(), Lang.NEED_OBJECTS.format(line), 1, 1);
	}
	
	
	public void serialize(Map<String, Object> map) {
		super.serialize(map);
		map.put("items", items);
		if (customMessage != null) map.put("customMessage", customMessage);
	}
	
	public static StageBringBack deserialize(Map<String, Object> map, QuestBranch branch) {
		StageBringBack st = new StageBringBack(branch, ((List<ItemStack>) map.get("items")).toArray(new ItemStack[0]), (String) map.getOrDefault("customMessage", null));
		st.loadDatas(map);
		return st;
	}

	public static class Creator extends StageNPC.AbstractCreator<StageBringBack> {
		
		private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageItems.toString());
		private static final ItemStack stageMessage = ItemUtils.item(XMaterial.PAPER, Lang.stageItemsMessage.toString());

		private List<ItemStack> items;
		private String message = null;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(5, stageItems, (p, item) -> {
				new ItemsGUI(items -> {
					setItems(items);
					reopenGUI(p, true);
				}, items).create(p);
			});
			line.setItem(9, stageMessage, (p, item) -> {
				new TextEditor<String>(p, () -> reopenGUI(p, false), x -> {
					setMessage(x);
					reopenGUI(p, false);
				}).passNullIntoEndConsumer().enter();
			});
		}
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			line.editItem(5, ItemUtils.lore(line.getItem(5), Lang.optionValue.format(this.items.size() + " item(s)")));
		}
		
		public void setMessage(String message) {
			this.message = message;
			line.editItem(9, ItemUtils.lore(line.getItem(9), message == null ? Lang.optionValue.format(Lang.NEED_OBJECTS.toString()) + " " + Lang.defaultValue.toString() : Lang.optionValue.format(message)));
		}
		
		@Override
		public void start(Player p) {
			new ItemsGUI(items -> {
				setItems(items);
				super.start(p);
			}, Collections.EMPTY_LIST).create(p);
		}

		@Override
		public void edit(StageBringBack stage) {
			super.edit(stage);
			setItems(Arrays.asList(stage.items));
			setMessage(stage.customMessage);
		}
		
		@Override
		protected StageBringBack createStage(QuestBranch branch) {
			return new StageBringBack(branch, items.toArray(new ItemStack[0]), message);
		}
	}

}
