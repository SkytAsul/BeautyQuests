package fr.skytasul.quests.gui.creation.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.api.stages.StageCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitBlockClick;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.BucketTypeGUI;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.misc.ItemGUI;
import fr.skytasul.quests.gui.mobs.MobsListGUI;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.stages.StageArea;
import fr.skytasul.quests.stages.StageBringBack;
import fr.skytasul.quests.stages.StageBucket;
import fr.skytasul.quests.stages.StageBucket.BucketType;
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
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.WorldGuard;
import fr.skytasul.quests.utils.types.BlockData;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.npc.NPC;

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
					StageCreator creator = StageCreator.creators.get(i);
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

	private void runClick(Line line, StageCreator creator, boolean branches){
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
		line.setItem(0, stageRemove.clone(), new StageRunnable() {
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
			line.setItem(15, ItemUtils.item(XMaterial.FILLED_MAP, Lang.newBranch.toString()), (p, datas, item) -> {
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
					StagesGUI branch = previousBranch;
					while (branch.previousBranch != null) branch = branch.previousBranch; // get the very first branch
					Inventories.create(p, branch);
				}
			}
		}else {
			stagesEdited = true;
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
		if (finish == null){
			finish = Inventories.create(p, edit != null ? new FinishGUI(this, edit, stagesEdited) : new FinishGUI(this));
		}else Inventories.create(p, finish);
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
		StageCreator.getCreator(stage.getType()).runnables.edit(line.data, stage);
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

		QuestsAPI.registerStage(new StageType("REGION", StageArea.class, Lang.Find.name(), "WorldGuard"), stageArea, new CreateArea());
		QuestsAPI.registerStage(new StageType("NPC", StageNPC.class, Lang.Talk.name()), stageNPC, new CreateNPC());
		QuestsAPI.registerStage(new StageType("ITEMS", StageBringBack.class, Lang.Items.name()), stageItems, new CreateBringBack());
		QuestsAPI.registerStage(new StageType("MOBS", StageMobs.class, Lang.Mobs.name()), stageMobs, new CreateMobs());
		QuestsAPI.registerStage(new StageType("MINE", StageMine.class, Lang.Mine.name()), stageMine, new CreateMine());
		QuestsAPI.registerStage(new StageType("CHAT", StageChat.class, Lang.Chat.name()), stageChat, new CreateChat());
		QuestsAPI.registerStage(new StageType("INTERACT", StageInteract.class, Lang.Interact.name()), stageInteract, new CreateInteract());
		QuestsAPI.registerStage(new StageType("FISH", StageFish.class, Lang.Fish.name()), stageFish, new CreateFish());
		QuestsAPI.registerStage(new StageType("CRAFT", StageCraft.class, Lang.Craft.name()), stageCraft, new CreateCraft());
		QuestsAPI.registerStage(new StageType("BUCKET", StageBucket.class, Lang.Bucket.name()), stageBucket, new CreateBucket());
		QuestsAPI.registerStage(new StageType("LOCATION", StageLocation.class, Lang.Location.name()), stageLocation, new CreateLocation());
	}
}






/*                         RUNNABLES                    */
class CreateNPC implements StageCreationRunnables{
	private static final ItemStack stageText = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Inventories.create(p, new SelectGUI((npc) -> {
			sg.reopen(p, true);
			npcDone(npc, sg, datas.getLine(), datas);
		}));
	}

	public static void npcDone(NPC npc, StagesGUI sg, Line line, LineData datas){
		datas.put("npc", npc);
		datas.getLine().setItem(6, stageText.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Utils.sendMessage(p, Lang.NPC_TEXT.toString());
				Editor.enterOrLeave(p, new DialogEditor(p, (NPC) datas.get("npc"), (obj) -> {
					sg.reopen(p, false);
					datas.put("npcText", obj);
				}, datas.containsKey("npcText") ? (Dialog) datas.get("npcText") : new Dialog((NPC) datas.get("npc"))));
			}
		}, true, true);

		datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.stageHide.toString(), datas.containsKey("hide") ? (boolean) datas.get("hide") : false), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				datas.put("hide", ItemUtils.toggle(item));
			}
		}, true, true);
	}

	public static void setFinish(StageNPC stage, LineData datas) {
		if (datas.containsKey("npcText")) stage.setDialog(datas.get("npcText"));
		if (datas.containsKey("hide")) stage.setHid((boolean) datas.get("hide"));
	}

	public static void setEdit(StageNPC stage, LineData datas) {
		if (stage.getDialog() != null) datas.put("npcText", new Dialog(stage.getDialog().getNPC(), stage.getDialog().messages.clone()));
		if (stage.isHid()) datas.put("hide", true);
		npcDone(stage.getNPC(), datas.getGUI(), datas.getLine(), datas);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch) {
		StageNPC stage = new StageNPC(branch, (NPC) datas.get("npc"));
		setFinish(stage, datas);
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageNPC st = (StageNPC) stage;
		setEdit(st, datas);
	}
}

class CreateBringBack implements StageCreationRunnables{
	private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageItems.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Line line = datas.getLine();
		setItem(line, sg);
		List<ItemStack> items = new ArrayList<>();
		datas.put("items", items);
		SelectGUI npcGUI = new SelectGUI((npc) -> {
			Inventories.closeWithoutExit(p);
			sg.reopen(p, true);
			if (npc != null) CreateNPC.npcDone(npc, sg, line, datas);
		});
		ItemsGUI itemsGUI = new ItemsGUI(() -> {
			Inventories.create(p, npcGUI);
		}, items);
		Inventories.create(p, itemsGUI);
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(7, stageItems.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Inventories.create(p, new ItemsGUI(() -> {
					sg.reopen(p, true);
				}, (List<ItemStack>) datas.get("items")));
			}
		});
	}

	public AbstractStage finish(LineData datas, QuestBranch branch) {
		StageBringBack stage = new StageBringBack(branch, (NPC) datas.get("npc"), ((List<ItemStack>) datas.get("items")).toArray(new ItemStack[0]));
		CreateNPC.setFinish(stage, datas);
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageBringBack st = (StageBringBack) stage;
		CreateNPC.setEdit(st, datas);
		datas.put("items", new ArrayList<>());
		((List<ItemStack>) datas.get("items")).addAll(Arrays.asList(st.getItems()));
		setItem(datas.getLine(), datas.getGUI());
	}
}

class CreateMobs implements StageCreationRunnables{
	private static final ItemStack editMobs = ItemUtils.item(XMaterial.STONE_SWORD, Lang.editMobs.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Line line = datas.getLine();
		MobsListGUI mobs = Inventories.create(p, new MobsListGUI());
		mobs.run = (obj) -> {
			sg.reopen(p, true);
			setItems(line, sg, datas);
			datas.put("mobs", obj);
		};
	}

	public static void setItems(Line line, StagesGUI sg, LineData datas){
		line.setItem(6, editMobs.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				MobsListGUI mobs = Inventories.create(p, new MobsListGUI());
				mobs.setMobsFromList((List<Mob<?>>) datas.get("mobs"));
				mobs.run = (obj) -> {
					sg.reopen(p, true);
					datas.put("mobs", obj);
				};
			}
		});
		line.setItem(5, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), datas.containsKey("shoot") ? (boolean) datas.get("shoot") : false), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				datas.put("shoot", ItemUtils.toggle(datas.getLine().getItem(5)));
			}
		});
	}

	public AbstractStage finish(LineData datas, QuestBranch branch) {
		StageMobs stage = new StageMobs(branch, ((List<Mob<?>>) datas.get("mobs")));
		if (datas.containsKey("shoot")) stage.setShoot((boolean) datas.get("shoot"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageMobs st = (StageMobs) stage;
		datas.put("mobs", new ArrayList<>(st.getMobs()));
		datas.put("shoot", st.isShoot());
		setItems(datas.getLine(), datas.getGUI(), datas);
	}
}

class CreateArea implements StageCreationRunnables{
	private static final ItemStack regionName = ItemUtils.item(XMaterial.PAPER, Lang.stageRegion.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Line line = datas.getLine();
		setItem(line, sg);
		launchRegionEditor(p, line, sg, datas, true);
	}
	private static void launchRegionEditor(Player p, Line line, StagesGUI sg, LineData datas, boolean first){
		Utils.sendMessage(p, Lang.REGION_NAME.toString() + (first ? "" : "\n" + Lang.TYPE_CANCEL.toString()));
		TextEditor wt = Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			String msg = (String) obj;
			if (WorldGuard.regionExists(msg, p.getWorld())) {
				sg.reopen(p, false);
				ItemUtils.name(line.getItem(6), msg);
				datas.put("region", msg);
				datas.put("world", p.getWorld().getName());
			} else {
				Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
				sg.reopen(p, false);
				if (first) line.click(0, p, line.getItem(0));
			}
		}));
		wt.cancel = () -> {
			sg.reopen(p, false);
			if (first) line.click(0, p, line.getItem(0));
		};
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(6, regionName.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				launchRegionEditor(p, line, sg, datas, false);
			}
		}, true, true);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch) {
		StageArea stage = new StageArea(branch, (String) datas.get("region"), (String) datas.get("world"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageArea st = (StageArea) stage;
		datas.put("region", st.getRegion().getId());
		datas.put("world", WorldGuard.getWorld(st.getRegion().getId()).getName());
		setItem(datas.getLine(), datas.getGUI());
		ItemUtils.name(datas.getLine().getItem(6), st.getRegion().getId());
	}
}

class CreateMine implements StageCreationRunnables{
	public void start(Player p, LineData datas){
		StagesGUI sg = datas.getGUI();
		BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
		blocks.run = (obj) -> {
			sg.reopen(p, true);
			datas.put("blocks", obj);
			datas.put("prevent", false);
			setItems(datas.getLine(), datas);
		};
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageMine stage = new StageMine(branch, (List<BlockData>) datas.get("blocks"));
		stage.setPlaceCancelled((boolean) datas.get("prevent"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageMine st = (StageMine) stage;
		datas.put("blocks", new ArrayList<>(st.getBlocks()));
		datas.put("prevent", st.isPlaceCancelled());
		setItems(datas.getLine(), datas);
	}

	public static void setItems(Line line, LineData datas){
		line.setItem(6, ItemUtils.item(XMaterial.STONE_PICKAXE, Lang.editBlocks.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
				blocks.setBlocksFromList(blocks.inv, (List<BlockData>) datas.get("blocks"));
				blocks.run = (obj) -> {
					datas.getGUI().reopen(p, true);
					datas.put("blocks", obj);
				};
			}
		});
		line.setItem(5, ItemUtils.itemSwitch(Lang.preventBlockPlace.toString(), (boolean) datas.get("prevent")), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				datas.put("prevent", ItemUtils.toggle(item));
			}
		});
	}
}

class CreateChat implements StageCreationRunnables{

	public void start(Player p, LineData datas){
		datas.put("cancel", true);
		setItems(datas);
		launchEditor(p, datas);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageChat stage = new StageChat(branch, (String) datas.get("text"), (boolean) datas.get("cancel"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageChat st = (StageChat) stage;
		datas.put("text", st.getText());
		datas.put("cancel", st.cancelEvent());
		setItems(datas);
	}

	public static void setItems(LineData datas) {
		datas.getLine().setItem(6, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				launchEditor(p, datas);
			}
		});
		datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), (boolean) datas.get("cancel")), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				datas.put("cancel", ItemUtils.toggle(item));
			}
		});
	}

	public static void launchEditor(Player p, LineData datas){
		Lang.CHAT_MESSAGE.send(p);
		new TextEditor(p, (obj) -> {
			datas.put("text", ((String) obj).replace("{SLASH}", "/"));
			datas.getGUI().reopen(p, false);
		}).enterOrLeave(p);
	}
}

class CreateInteract implements StageCreationRunnables{

	public void start(Player p, LineData datas){
		Lang.CLICK_BLOCK.send(p);
		new WaitBlockClick(p, (obj) -> {
			datas.put("lc", obj);
			datas.getGUI().reopen(p, false);
			setItems(datas);
		}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
	}
	
	public static void setItems(LineData datas){
		datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.leftClick.toString(), datas.containsKey("left") ? (boolean) datas.get("left") : false), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				datas.put("left", ItemUtils.toggle(item));
			}
		});
		datas.getLine().setItem(6, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Lang.CLICK_BLOCK.send(p);
				new WaitBlockClick(p, (obj) -> {
					datas.getGUI().reopen(p, false);
					datas.put("lc", obj);
				}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
			}
		});
	}

	public void edit(LineData datas, AbstractStage stage){
		StageInteract st = (StageInteract) stage;
		datas.put("lc", st.getLocation());
		datas.put("left", st.needLeftClick());
		setItems(datas);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		return new StageInteract(branch, (Location) datas.get("lc"), datas.containsKey("left") ? (boolean) datas.get("left") : false);
	}
	
}

class CreateFish implements StageCreationRunnables{
	public void start(Player p, LineData datas){
		List<ItemStack> items = new ArrayList<>();
		datas.put("items", items);
		Inventories.create(p, new ItemsGUI(() -> {
			datas.getGUI().reopen(p, true);
			setItem(datas.getLine(), datas.getGUI());
		}, items));
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageFish stage = new StageFish(branch, ((List<ItemStack>) datas.get("items")).toArray(new ItemStack[0]));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		datas.put("items", new ArrayList<>(Arrays.asList(((StageFish) stage).getFishes())));
		setItem(datas.getLine(), datas.getGUI());
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(6, ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Inventories.create(p, new ItemsGUI(() -> {
					datas.getGUI().reopen(p, true);
				}, (List<ItemStack>) datas.get("items")));
			}
		});
	}
}

class CreateCraft implements StageCreationRunnables{
	public void start(Player p, LineData datas){
		new ItemGUI((is) -> {
			datas.put("item", is);
			datas.getGUI().reopen(p, true);
			setItem(datas.getLine());
		}).create(p);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageCraft stage = new StageCraft(branch, (ItemStack) datas.get("item"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		datas.put("item", ((StageCraft) stage).getItem());
		setItem(datas.getLine());
	}

	public static void setItem(Line line){
		line.setItem(6, ItemUtils.item(XMaterial.CHEST, Lang.editItem.toString(), ItemUtils.getName(((ItemStack) line.data.get("item")))), (p, datas, item) -> {
			new ItemGUI((is) -> {
				datas.put("item", is);
				datas.getGUI().reopen(p, true);
			}).create(p);
		});
	}
}

class CreateBucket implements StageCreationRunnables{
	public void start(Player p, LineData datas){
		new BucketTypeGUI((bucket) -> {
			datas.put("bucket", bucket);
			Lang.BUCKET_AMOUNT.send(p);
			new TextEditor(p, (obj) -> {
				datas.put("amount", obj);
				datas.getGUI().reopen(p, true);
				setItems(datas.getLine());
			}, new NumberParser(Integer.class, true, true)).enterOrLeave(p);
		}).create(p);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageBucket stage = new StageBucket(branch, (BucketType) datas.get("bucket"), (int) datas.get("amount"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageBucket st = (StageBucket) stage;
		datas.put("bucket", st.getBucketType());
		datas.put("amount", st.getBucketAmount());
		setItems(datas.getLine());
	}

	public static void setItems(Line line){
		line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.editBucketAmount.toString(), Lang.Amount.format(line.data.get("amount"))), (p, datas, item) -> {
			Lang.BUCKET_AMOUNT.send(p);
			new TextEditor(p, (obj) -> {
				datas.put("amount", obj);
				datas.getGUI().reopen(p, true);
				ItemUtils.lore(item, Lang.Amount.format(obj));
			}, new NumberParser(Integer.class, true, true)).enterOrLeave(p);
		});
		BucketType type = (BucketType) line.data.get("bucket");
		line.setItem(6, ItemUtils.item(type.getMaterial(), Lang.editBucketType.toString(), type.getName()), (p, datas, item) -> {
			new BucketTypeGUI((bucket) -> {
				datas.put("bucket", bucket);
				datas.getGUI().reopen(p, true);
				item.setType(bucket.getMaterial().parseMaterial());
				ItemUtils.lore(item, bucket.getName());
			}).create(p);
		});
	}
}

class CreateLocation implements StageCreationRunnables{
	public void start(Player p, LineData datas){
		Lang.LOCATION_GO.send(p);
		new WaitClick(p, NPCGUI.validMove, () -> {
			datas.put("location", p.getLocation());
			datas.put("radius", 5);
			datas.getGUI().reopen(p, false);
			setItems(datas.getLine());
		}).enterOrLeave(p);
	}

	public AbstractStage finish(LineData datas, QuestBranch branch){
		StageLocation stage = new StageLocation(branch, (Location) datas.get("location"), (int) datas.get("radius"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageLocation st = (StageLocation) stage;
		datas.put("location", st.getLocation());
		datas.put("radius", st.getRadius());
		setItems(datas.getLine());
	}

	public static void setItems(Line line){
		line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.editRadius.toString(), Lang.currentRadius.format(line.data.get("radius"))), (p, datas, item) -> {
			Lang.LOCATION_RADIUS.send(p);
			new TextEditor(p, (x) -> {
				datas.put("radius", x);
				datas.getGUI().reopen(p, false);
				ItemUtils.lore(item, Lang.currentRadius.format(x));
			}, new NumberParser(Integer.class, true, true), () -> datas.getGUI().reopen(p, false), null).enterOrLeave(p);
		});
		line.setItem(6, ItemUtils.item(XMaterial.STICK, Lang.editLocation.toString()), (p, datas, item) -> {
			Lang.LOCATION_GO.send(p);
			new WaitClick(p, NPCGUI.validMove, () -> {
				datas.put("location", p.getLocation());
				datas.getGUI().reopen(p, false);
			}).enterOrLeave(p);
		});
	}
}