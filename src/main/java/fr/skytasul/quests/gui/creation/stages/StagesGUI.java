package fr.skytasul.quests.gui.creation.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.api.stages.StageCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitBlockClick;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.mobs.MobsListGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.stages.StageArea;
import fr.skytasul.quests.stages.StageBringBack;
import fr.skytasul.quests.stages.StageChat;
import fr.skytasul.quests.stages.StageFish;
import fr.skytasul.quests.stages.StageInteract;
import fr.skytasul.quests.stages.StageMine;
import fr.skytasul.quests.stages.StageMobs;
import fr.skytasul.quests.stages.StageNPC;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.WorldGuard;
import fr.skytasul.quests.utils.types.BlockData;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.Mob;
import fr.skytasul.quests.utils.types.RunnableObj;
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

	public Inventory inv;
	int page;
	private boolean stop = false;

	public Inventory open(Player p) {
		if (inv == null){
			inv = Bukkit.createInventory(null, 54, Lang.INVENTORY_STAGES.toString());

			page = 0;
			lines.add(new Line(inv, 0, this));
			setStageCreate(lines.get(0));
			DebugUtils.debugMessage(p, "First line initialized.");
			for (int i = 1; i < 15; i++) lines.add(new Line(inv, i, this));
			DebugUtils.debugMessage(p, lines.size() + " lines created.");

			inv.setItem(45, ItemUtils.itemLaterPage());
			inv.setItem(49, ItemUtils.itemNextPage());

			inv.setItem(51, ItemUtils.itemDone());
			inv.setItem(52, ItemUtils.itemCancel());
			refresh(p);
		}

		p.openInventory(inv);
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
			DebugUtils.debugMessage(p, "open");
			if (reImplement) Inventories.put(p, this, inv);
			p.openInventory(inv);
		}
		return this;
	}

	private void setStageCreate(Line line){
		line.removeItems();
		line.setFirst(stageCreate.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				line.setFirst(null, null);
				int i = 0;
				for (Entry<StageType, StageCreator> en : StageCreator.getCreators().entrySet()){
					line.setItem(i, en.getValue().item, new StageRunnable() {
						public void run(Player p, LineData datas, ItemStack item) {
							runClick(line, datas, en.getKey());
							en.getValue().runnables.start(p, datas);
						}
					}, true, false);
					datas.put(i + "", en.getKey());
					i++;
				}
				line.setItems(0);
			}
		});
		line.setItems(0);
	}

	private void runClick(Line line, LineData datas, StageType type){
		line.removeItems();
		datas.clear();
		datas.put("type", type);
		datas.put("rewards", new ArrayList<>());

		line.setItem(0, ending, new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Inventories.create(p, new RewardsGUI(new RunnableObj() {
					public void run(Object obj){
						datas.put("rewards", obj);
						reopen(p, true);
					}
				}, (List<AbstractReward>) datas.get("rewards")));
			}
		});

		line.setItem(1, descMessage.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Lang.DESC_MESSAGE.send(p);
				TextEditor text = Editor.enterOrLeave(p, new TextEditor(p, new RunnableObj() {
					public void run(Object obj){
						datas.put("customText", obj);
						line.editItem(1, ItemUtils.lore(line.getItem(1), (String) obj));
						reopen(p, false);
					}
				}));
				text.nul = () -> {
						datas.remove("customText");
						line.editItem(1,  ItemUtils.lore(line.getItem(1)));
						reopen(p, false);
				};
				text.cancel = () -> {
						reopen(p, false);
				};
			}
		});

		line.setItem(2, startMessage.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				Lang.START_TEXT.send(p);
				TextEditor text = Editor.enterOrLeave(p, new TextEditor(p, new RunnableObj() {
					public void run(Object obj){
						datas.put("startMessage", obj);
						line.editItem(2, ItemUtils.lore(line.getItem(2), (String) obj));
						reopen(p, false);
					}
				}));
				text.nul = () -> {
						datas.remove("startMessage");
						line.editItem(2, ItemUtils.lore(line.getItem(2)));
						reopen(p, false);
				};
				text.cancel = () -> {
						reopen(p, false);
				};
			}
		});

		line.setFirst(stageRemove.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				datas.clear();
				line.removeItems();
				if (line.getLine() != 14){
					for (int i = line.getLine() + 1; i < 15; i++){
						getLine(i).exchangeLines(getLine(i - 1));
					}
					DebugUtils.debugMessage(p, "--");
				}
				for (int i = 0; i < 15; i++){
					Line l = getLine(i);
					if (!isActiveLine(l)){
						if (isClearLine(l)) break;
						setStageCreate(l);
						break;
					}
				}
			}
		});

		if (line.getLine() != 14){
			Line next = getLine(line.getLine() + 1);
			if (!next.data.containsKey("type")) setStageCreate(next);
		}
	}

	private boolean isActiveLine(Line line){
		return line.data.containsKey("type");
	}

	private boolean isClearLine(Line line){
		return !isActiveLine(line) && line.first == null;
	}

	public Line getLine(int id){
		for (Line l : lines){
			if (l.getLine() == id) return l;
		}
		return null;
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot > 44) {
			if (slot == 45) {
				if (page > 0) {
					page--;
					refresh(p);
				}
			}else if (slot == 49) {
				if (page < 2) {
					page++;
					refresh(p);
				}
			}else if (slot == 51) {
				if (isActiveLine(getLine(0))) finish(p);
			}else if (slot == 52) {
				stop = true;
				p.closeInventory();
				if (isActiveLine(getLine(0))) {
					if (edit == null) {
						Lang.QUEST_CANCEL.send(p);
					}else Lang.QUEST_EDIT_CANCEL.send(p);
				}
			}
		}else {
			stagesEdited = true;
			Line line = getLine(Line.getLineNumber(slot)/9 +5*page);
			line.click(slot, p, current);
		}
		return true;
	}

	public CloseBehavior onClose(Player p, Inventory inv){
		if (!isActiveLine(getLine(0)) || stop) return CloseBehavior.REMOVE;
		return CloseBehavior.REOPEN;
	}

	private void refresh(Player p) {
		for (int i = 0; i < 3; i++) inv.setItem(i + 46, ItemUtils.itemSeparator(i == page ? DyeColor.GREEN : DyeColor.GRAY));
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
		for (int i = 0; i < 15; i++){
			if (isActiveLine(getLine(i))) lines.add(/*i, */getLine(i).data);
		}
		return lines;
	}

	public void edit(Quest quest){
		for (AbstractStage st : quest.getStageManager().getStages()){
			Line line = getLine(st.getID());
			runClick(line, line.data, st.getType());
			//line.data.put("end", st.getEnding().clone());
			line.data.put("rewards", st.getRewards());
			if (st.getStartMessage() != null){
				line.data.put("startMessage", st.getStartMessage());
				line.editItem(2, ItemUtils.lore(line.getItem(2), st.getStartMessage()));
			}
			if (st.getCustomText() != null){
				line.data.put("customText", st.getCustomText());
				line.editItem(1, ItemUtils.lore(line.getItem(1), st.getCustomText()));
			}
			StageCreator.getCreators().get(st.getType()).runnables.edit(line.data, st);
			line.setItems(0);
		}
		edit = quest;
	}



	private static final ItemStack stageNPC = ItemUtils.item(XMaterial.OAK_SIGN, Lang.stageNPC.toString());
	private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageBring.toString());
	private static final ItemStack stageArea = ItemUtils.item(XMaterial.WOODEN_AXE, Lang.stageGoTo.toString());
	private static final ItemStack stageMobs = ItemUtils.item(XMaterial.WOODEN_SWORD, Lang.stageMobs.toString());
	private static final ItemStack stageMine = ItemUtils.item(XMaterial.WOODEN_PICKAXE, Lang.stageMine.toString());
	private static final ItemStack stageChat = ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.stageChat.toString());
	private static final ItemStack stageInteract = ItemUtils.item(XMaterial.OAK_PLANKS, Lang.stageInteract.toString());
	private static final ItemStack stageFish = ItemUtils.item(XMaterial.COD, Lang.stageFish.toString());

	public static void initialize(){
		DebugUtils.broadcastDebugMessage("Initlializing default stage types.");

		QuestsAPI.registerStage(new StageType("REGION", StageArea.class, Lang.Find.name(), "WorldGuard"), stageArea, new CreateArea());
		QuestsAPI.registerStage(new StageType("NPC", StageNPC.class, Lang.Talk.name()), stageNPC, new CreateNPC());
		QuestsAPI.registerStage(new StageType("ITEMS", StageBringBack.class, Lang.Items.name()), stageItems, new CreateBringBack());
		QuestsAPI.registerStage(new StageType("MOBS", StageMobs.class, Lang.Mobs.name()), stageMobs, new CreateMobs());
		QuestsAPI.registerStage(new StageType("MINE", StageMine.class, Lang.Mine.name()), stageMine, new CreateMine());
		QuestsAPI.registerStage(new StageType("CHAT", StageChat.class, Lang.Chat.name()), stageChat, new CreateChat());
		QuestsAPI.registerStage(new StageType("INTERACT", StageInteract.class, Lang.Interact.name()), stageInteract, new CreateInteract());
		QuestsAPI.registerStage(new StageType("FISH", StageFish.class, Lang.Fish.name()), stageFish, new CreateFish());
	}
}






/*                         RUNNABLES                    */
class CreateNPC implements StageCreationRunnables{
	private static final ItemStack stageText = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Inventories.create(p, new SelectGUI((obj) -> {
			sg.reopen(p, true);
			npcDone((NPC) obj, sg, datas.getLine(), datas);
		}));
	}

	public static void npcDone(NPC npc, StagesGUI sg, Line line, LineData datas){
		datas.put("npc", npc);
		datas.getLine().setItem(5, stageText.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Utils.sendMessage(p, Lang.NPC_TEXT.toString());
				Editor.enterOrLeave(p, new DialogEditor(p, (NPC) datas.get("npc"), (obj) -> {
					sg.reopen(p, false);
					datas.put("npcText", obj);
				}, datas.containsKey("npcText") ? (Dialog) datas.get("npcText") : new Dialog((NPC) datas.get("npc"))));
			}
		}, true, true);

		datas.getLine().setItem(4, ItemUtils.itemSwitch(Lang.stageHide.toString(), datas.containsKey("hide") ? (boolean) datas.get("hide") : false), new StageRunnable() {
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

	public AbstractStage finish(LineData datas, Quest qu) {
		StageNPC stage = new StageNPC(qu.getStageManager(), (NPC) datas.get("npc"));
		setFinish(stage, datas);
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageNPC st = (StageNPC) stage;
		setEdit(st, datas);
	}
}

class CreateBringBack implements StageCreationRunnables{
	private static final ItemStack stageItems = ItemUtils.item(XMaterial.DIAMOND_SWORD, Lang.stageItems.toString());
	public void start(Player p, LineData datas) {
		StagesGUI sg = datas.getGUI();
		Line line = datas.getLine();
		setItem(line, sg);
		List<ItemStack> items = new ArrayList<>();
		datas.put("items", items);
		SelectGUI npcGUI = new SelectGUI((obj) -> {
			Inventories.closeWithoutExit(p);
			sg.reopen(p, true);
			if (obj != null) CreateNPC.npcDone((NPC) obj, sg, line, datas);
		});
		ItemsGUI itemsGUI = new ItemsGUI(() -> {
			Inventories.create(p, npcGUI);
		}, items);
		Inventories.create(p, itemsGUI);
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(6, stageItems.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Inventories.create(p, new ItemsGUI(() -> {
					sg.reopen(p, true);
				}, (List<ItemStack>) datas.get("items")));
			}
		});
	}

	public AbstractStage finish(LineData datas, Quest qu) {
		StageBringBack stage = new StageBringBack(qu.getStageManager(), (NPC) datas.get("npc"), ((List<ItemStack>) datas.get("items")).toArray(new ItemStack[0]));
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
		line.setItem(5, editMobs.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				MobsListGUI mobs = Inventories.create(p, new MobsListGUI());
				mobs.setMobsFromList((List<Mob>) datas.get("mobs"));
				mobs.run = (obj) -> {
					sg.reopen(p, true);
					datas.put("mobs", obj);
				};
			}
		});
		line.setItem(6, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), datas.containsKey("shoot") ? (boolean) datas.get("shoot") : false), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				datas.put("shoot", ItemUtils.toggle(datas.getLine().getItem(6)));
			}
		});
	}

	public AbstractStage finish(LineData datas, Quest qu) {
		StageMobs stage = new StageMobs(qu.getStageManager(), ((List<Mob>) datas.get("mobs")));
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
				if (first) line.executeFirst(p);
			}
		}));
		wt.cancel = () -> {
				sg.reopen(p, false);
				if (first) line.executeFirst(p);
		};
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(6, regionName.clone(), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				launchRegionEditor(p, line, sg, datas, false);
			}
		}, true, true);
	}

	public AbstractStage finish(LineData datas, Quest qu) {
		StageArea stage = new StageArea(qu.getStageManager(), (String) datas.get("region"), (String) datas.get("world"));
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

	public AbstractStage finish(LineData datas, Quest qu){
		StageMine stage = new StageMine(qu.getStageManager(), (List<BlockData>) datas.get("blocks"));
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
		line.setItem(5, ItemUtils.item(XMaterial.STONE_PICKAXE, Lang.editBlocks.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
				blocks.setBlocksFromList(blocks.inv, (List<BlockData>) datas.get("blocks"));
				blocks.run = (obj) -> {
					datas.getGUI().reopen(p, true);
					datas.put("blocks", obj);
				};
			}
		});
		line.setItem(4, ItemUtils.itemSwitch(Lang.preventBlockPlace.toString(), (boolean) datas.get("prevent")), new StageRunnable() {
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

	public AbstractStage finish(LineData datas, Quest qu){
		StageChat stage = new StageChat(qu.getStageManager(), (String) datas.get("text"), (boolean) datas.get("cancel"));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		StageChat st = (StageChat) stage;
		datas.put("text", st.getText());
		datas.put("cancel", st.cancelEvent());
		setItems(datas);
	}

	public static void setItems(LineData datas) {
		datas.getLine().setItem(5, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				launchEditor(p, datas);
			}
		});
		datas.getLine().setItem(4, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), (boolean) datas.get("cancel")), new StageRunnable() {
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
		datas.getLine().setItem(4, ItemUtils.itemSwitch(Lang.leftClick.toString(), datas.containsKey("left") ? (boolean) datas.get("left") : false), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item){
				datas.put("left", ItemUtils.toggle(item));
			}
		});
		datas.getLine().setItem(5, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString()), new StageRunnable() {
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

	public AbstractStage finish(LineData datas, Quest qu){
		return new StageInteract(qu.getStageManager(), (Location) datas.get("lc"), datas.containsKey("left") ? (boolean) datas.get("left") : false);
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

	public AbstractStage finish(LineData datas, Quest qu){
		StageFish stage = new StageFish(qu.getStageManager(), ((List<ItemStack>) datas.get("items")).toArray(new ItemStack[0]));
		return stage;
	}

	public void edit(LineData datas, AbstractStage stage){
		datas.put("items", new ArrayList<>(Arrays.asList(((StageFish) stage).getFishes())));
		setItem(datas.getLine(), datas.getGUI());
	}

	public static void setItem(Line line, StagesGUI sg){
		line.setItem(5, ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString()), new StageRunnable() {
			public void run(Player p, LineData datas, ItemStack item) {
				Inventories.create(p, new ItemsGUI(() -> {
					datas.getGUI().reopen(p, true);
				}, (List<ItemStack>) datas.get("items")));
			}
		});
	}
}