package fr.skytasul.quests.gui.creation.stages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.stages.StageArea;
import fr.skytasul.quests.stages.StageBringBack;
import fr.skytasul.quests.stages.StageBucket;
import fr.skytasul.quests.stages.StageChat;
import fr.skytasul.quests.stages.StageCraft;
import fr.skytasul.quests.stages.StageFish;
import fr.skytasul.quests.stages.StageInteract;
import fr.skytasul.quests.stages.StageLocation;
import fr.skytasul.quests.stages.StageMine;
import fr.skytasul.quests.stages.StageMobs;
import fr.skytasul.quests.stages.StageNPC;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class StagesGUI implements CustomInventory {

	private static final ItemStack stageCreate = ItemUtils.item(XMaterial.SLIME_BALL, Lang.stageCreate.toString());
	private static final ItemStack stageRemove = ItemUtils.item(XMaterial.BARRIER, Lang.stageRemove.toString());

	public static final ItemStack ending = ItemUtils.item(XMaterial.BAKED_POTATO, Lang.ending.toString());
	private static final ItemStack descMessage = ItemUtils.item(XMaterial.OAK_SIGN, Lang.descMessage.toString());
	private static final ItemStack startMessage = ItemUtils.item(XMaterial.FEATHER, Lang.startMsg.toString());

	private List<Line> lines = new ArrayList<>();
	
	private Quest edit;
	private boolean stagesEdited = false;

	private FinishGUI finish = null;
	private StagesGUI previousBranch;

	public Inventory inv;
	int page;
	private boolean stop = false;

	public StagesGUI(StagesGUI previousBranch){
		this.previousBranch = previousBranch;
	}
	
	public Inventory open(Player p) {
		if (inv == null){
			inv = Bukkit.createInventory(null, 54, Lang.INVENTORY_STAGES.toString());

			page = 0;
			for (int i = 0; i < 20; i++) lines.add(new Line(inv, i, this));
			setStageCreate(lines.get(0), false);
			setStageCreate(lines.get(15), true);

			inv.setItem(45, ItemUtils.itemLaterPage);
			inv.setItem(50, ItemUtils.itemNextPage);

			inv.setItem(52, ItemUtils.itemDone);
			inv.setItem(53, previousBranch == null ? ItemUtils.itemCancel : ItemUtils.item(XMaterial.FILLED_MAP, Lang.previousBranch.toString()));
			refresh();
		}

		if (p != null) inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	/**
	 * Get the StagesGUI, open it for player if specified, and re implement the player in the inventories system if on true
	 * @param p player to open (can be null)
	 * @param reImplement re implement the player in the inventories system
	 * @return this StagesGUI
	 */
	public StagesGUI reopen(Player p, boolean reImplement){
		if (p != null){
			if (reImplement) Inventories.put(p, this, inv);
			p.openInventory(inv);
		}
		return this;
	}

	private void setStageCreate(Line line, boolean branches){
		line.removeItems();
		line.setItem(0, stageCreate.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				line.setItem(0, null, null, true, false);
				for (int i = 0; i < StageCreator.creators.size(); i++) {
					StageCreator<?> creator = StageCreator.creators.get(i);
					line.setItem(i + 1, creator.item, new StageRunnable() {
						public void run(Player p, LineData datas, ItemStack item) {
							line.data.clear();
							runClick(line, creator, branches);
							creator.runnables.start(p, datas);
						}
					}, true, false);
				}
				line.setItems(0);
			}
		});
		line.setItems(0);
	}

	private void runClick(Line line, StageCreator<?> creator, boolean branches) {
		line.removeItems();
		line.data.put("type", creator.type);
		line.data.put("rewards", new ArrayList<>());

		line.setItem(1, ending, new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Inventories.create(p, new RewardsGUI((rewards) -> {
					datas.put("rewards", rewards);
					reopen(p, true);
				}, (List<AbstractReward>) datas.get("rewards")));
			}
		});

		line.setItem(2, descMessage.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Lang.DESC_MESSAGE.send(p);
				TextEditor text = Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
					datas.put("customText", obj);
					line.editItem(2, ItemUtils.lore(line.getItem(2), (String) obj));
					reopen(p, false);
				}));
				text.nul = () -> {
					datas.remove("customText");
					line.editItem(2,  ItemUtils.lore(line.getItem(2)));
					reopen(p, false);
				};
				text.cancel = () -> {
					reopen(p, false);
				};
			}
		});

		line.setItem(3, startMessage.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Lang.START_TEXT.send(p);
				TextEditor text = Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
					datas.put("startMessage", obj);
					line.editItem(3, ItemUtils.lore(line.getItem(3), (String) obj));
					reopen(p, false);
				}));
				text.nul = () -> {
					datas.remove("startMessage");
					line.editItem(3, ItemUtils.lore(line.getItem(3)));
					reopen(p, false);
				};
				text.cancel = () -> {
					reopen(p, false);
				};
			}
		});

		int maxStages = branches ? 20 : 15;
		line.setItem(0, ItemUtils.lore(stageRemove.clone(), creator.type.name), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				datas.clear();
				line.removeItems();
				if (line.getLine() != maxStages-1){
					for (int i = line.getLine() + 1; i < maxStages; i++){
						getLine(i).exchangeLines(getLine(i - 1));
					}
				}
				for (int i = 0; i < maxStages; i++){
					Line l = getLine(i);
					if (!isActiveLine(l)){
						if (!isActiveLine(l)) break;
						setStageCreate(l, i > maxStages-1);
						break;
					}
				}
			}
		});

		if (line.getLine() != maxStages-1){
			Line next = getLine(line.getLine() + 1);
			if (!next.data.containsKey("type")) setStageCreate(next, branches);
		}
		
		if (branches){
			if (!line.data.containsKey("branch")) line.data.put("branch", new StagesGUI(this));
			line.setItem(14, ItemUtils.item(XMaterial.FILLED_MAP, Lang.newBranch.toString()), (p, datas, item) -> {
				Inventories.create(p, (StagesGUI) datas.get("branch"));
			});
		}
	}

	private boolean isActiveLine(Line line){
		return line.data.containsKey("type");
	}

	public Line getLine(int id){
		for (Line l : lines){
			if (l.getLine() == id) return l;
		}
		return null;
	}
	
	public boolean isEmpty(){
		if (lines.isEmpty()) return true; // if this StagesGUI has never been opened
		return !isActiveLine(getLine(0)) && !isActiveLine(getLine(15));
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot > 44) {
			if (slot == 45) {
				if (page > 0) {
					page--;
					refresh();
				}
			}else if (slot > 45 && slot < 50){
				page = slot - 46;
				refresh();
			}else if (slot == 50) {
				if (page < 3) {
					page++;
					refresh();
				}
			}else if (slot == 52) {
				if (previousBranch == null){ // main inventory = directly finish if not empty
					if (!isEmpty()) finish(p);
				}else { // branch inventory = get the main inventory to finish
					StagesGUI branch = previousBranch;
					while (branch.previousBranch != null) branch = branch.previousBranch; // get the very first branch
					branch.finish(p);
				}
			}else if (slot == 53) {
				if (previousBranch == null){ // main inventory = cancel button
					stop = true;
					p.closeInventory();
					if (!isEmpty()) {
						if (edit == null) {
							Lang.QUEST_CANCEL.send(p);
						}else Lang.QUEST_EDIT_CANCEL.send(p);
					}
				}else { // branch inventory = previous branch button
					Inventories.create(p, previousBranch);
				}
			}
		}else {
			StagesGUI branch = this;
			while (branch.previousBranch != null) branch = branch.previousBranch; // get the very first branch
			branch.stagesEdited = true;
			Line line = getLine((slot - slot % 9)/9 +5*page);
			line.click(slot, p, current);
		}
		return true;
	}

	public CloseBehavior onClose(Player p, Inventory inv){
		if (isEmpty() || stop) return CloseBehavior.REMOVE;
		return CloseBehavior.REOPEN;
	}

	private void refresh() {
		for (int i = 0; i < 3; i++) inv.setItem(i + 46, ItemUtils.item(i == page ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.WHITE_STAINED_GLASS_PANE, Lang.regularPage.toString()));
		inv.setItem(49, ItemUtils.item(page == 3 ? XMaterial.MAGENTA_STAINED_GLASS_PANE : XMaterial.PURPLE_STAINED_GLASS_PANE, Lang.branchesPage.toString()));
		
		for (Line line : lines) {
			line.setItems(line.getActivePage());
		}
	}

	private void finish(Player p){
		System.out.println(stagesEdited);
		if (finish == null){
			finish = Inventories.create(p, edit != null ? new FinishGUI(this, edit, stagesEdited) : new FinishGUI(this));
		}else {
			Inventories.create(p, finish);
			if (edit != null && stagesEdited) finish.setStagesEdited();
		}
	}

	public List<LineData> getLinesDatas(){
		List<LineData> lines = new LinkedList<>();
		for (int i = 0; i < 20; i++){
			if (isActiveLine(getLine(i))) lines.add(getLine(i).data);
		}
		return lines;
	}

	public void edit(Quest quest){
		edit = quest;
		editBranch(quest.getBranchesManager().getBranch(0));
	}
	
	private void editBranch(QuestBranch branch){
		for (AbstractStage stage : branch.getRegularStages()){
			Line line = getLine(stage.getID());
			runClick(line, StageCreator.getCreator(stage.getType()), false);
			stageDatas(line, stage);
		}
		
		int i = 15;
		for (Entry<AbstractStage, QuestBranch> en : branch.getEndingStages().entrySet()){
			Line line = getLine(i);
			StagesGUI gui = new StagesGUI(this);
			gui.open(null); // init other GUI
			line.data.put("branch", gui);
			if (en.getValue() != null) gui.editBranch(en.getValue());
			runClick(line, StageCreator.getCreator(en.getKey().getType()), true);
			stageDatas(line, en.getKey());
			i++;
		}
	}
	
	private void stageDatas(Line line, AbstractStage stage){
		line.data.put("rewards", stage.getRewards());
		if (stage.getStartMessage() != null){
			line.data.put("startMessage", stage.getStartMessage());
			line.editItem(3, ItemUtils.lore(line.getItem(3), stage.getStartMessage()));
		}
		if (stage.getCustomText() != null){
			line.data.put("customText", stage.getCustomText());
			line.editItem(2, ItemUtils.lore(line.getItem(2), stage.getCustomText()));
		}
		@SuppressWarnings ("rawtypes")
		StageCreator creator = StageCreator.getCreator(stage.getType());
		creator.runnables.edit(line.data, stage);
		line.setItems(0);
	}



	private static final ItemStack stageNPC = ItemUtils.item(XMaterial.OAK_SIGN, Lang.stageNPC.toString());
	private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageBring.toString());
	private static final ItemStack stageArea = ItemUtils.item(XMaterial.WOODEN_AXE, Lang.stageGoTo.toString());
	private static final ItemStack stageMobs = ItemUtils.item(XMaterial.WOODEN_SWORD, Lang.stageMobs.toString());
	private static final ItemStack stageMine = ItemUtils.item(XMaterial.WOODEN_PICKAXE, Lang.stageMine.toString());
	private static final ItemStack stageChat = ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.stageChat.toString());
	private static final ItemStack stageInteract = ItemUtils.item(XMaterial.OAK_PLANKS, Lang.stageInteract.toString());
	private static final ItemStack stageFish = ItemUtils.item(XMaterial.COD, Lang.stageFish.toString());
	private static final ItemStack stageCraft = ItemUtils.item(XMaterial.CRAFTING_TABLE, Lang.stageCraft.toString());
	private static final ItemStack stageBucket = ItemUtils.item(XMaterial.BUCKET, Lang.stageBucket.toString());
	private static final ItemStack stageLocation = ItemUtils.item(XMaterial.MINECART, Lang.stageLocation.toString());

	public static void initialize(){
		DebugUtils.logMessage("Initlializing default stage types.");

		QuestsAPI.registerStage(new StageType("REGION", StageArea.class, Lang.Find.name(), "WorldGuard"), stageArea, new StageArea.Creator());
		QuestsAPI.registerStage(new StageType("NPC", StageNPC.class, Lang.Talk.name()), stageNPC, new StageNPC.Creator());
		QuestsAPI.registerStage(new StageType("ITEMS", StageBringBack.class, Lang.Items.name()), stageItems, new StageBringBack.Creator());
		QuestsAPI.registerStage(new StageType("MOBS", StageMobs.class, Lang.Mobs.name()), stageMobs, new StageMobs.Creator());
		QuestsAPI.registerStage(new StageType("MINE", StageMine.class, Lang.Mine.name()), stageMine, new StageMine.Creator());
		QuestsAPI.registerStage(new StageType("CHAT", StageChat.class, Lang.Chat.name()), stageChat, new StageChat.Creator());
		QuestsAPI.registerStage(new StageType("INTERACT", StageInteract.class, Lang.Interact.name()), stageInteract, new StageInteract.Creator());
		QuestsAPI.registerStage(new StageType("FISH", StageFish.class, Lang.Fish.name()), stageFish, new StageFish.Creator());
		QuestsAPI.registerStage(new StageType("CRAFT", StageCraft.class, Lang.Craft.name()), stageCraft, new StageCraft.Creator());
		QuestsAPI.registerStage(new StageType("BUCKET", StageBucket.class, Lang.Bucket.name()), stageBucket, new StageBucket.Creator());
		QuestsAPI.registerStage(new StageType("LOCATION", StageLocation.class, Lang.Location.name()), stageLocation, new StageLocation.Creator());
	}
}