package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.events.QuestCreateEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.npc.NPC;

public class FinishGUI implements CustomInventory{

	static ItemStack multiple = ItemUtils.itemSwitch(Lang.multiple.toString(), false, Lang.multipleLore.toString());
	static ItemStack scoreboard = ItemUtils.itemSwitch(Lang.scoreboard.toString(), true);
	static ItemStack hide = ItemUtils.itemSwitch(Lang.hide.toString(), false);
	static ItemStack bypass = ItemUtils.itemSwitch(Lang.bypass.toString(), false);
	static ItemStack questName = ItemUtils.item(XMaterial.NAME_TAG, Lang.questName.toString());
	static ItemStack questStarterCreate = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.questStarterCreate.toString());
	static ItemStack questStarterSelect = ItemUtils.item(XMaterial.STICK, Lang.questStarterSelect.toString());
	static ItemStack create = ItemUtils.item(XMaterial.NETHER_BRICK, ChatColor.DARK_PURPLE + Lang.create.toString(), Lang.createLore.toString().split("\n"));
	static ItemStack edit = ItemUtils.item(XMaterial.NETHER_BRICK, ChatColor.DARK_PURPLE + Lang.edit.toString(), Lang.createLore.toString().split("\n"));
	static ItemStack editRequirements = ItemUtils.item(XMaterial.ACACIA_DOOR, Lang.editRequirements.toString());
	static ItemStack endMessage = ItemUtils.item(XMaterial.PAPER, Lang.endMessage.toString());
	static ItemStack startDialog = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.startDialog.toString());
	static ItemStack startRewards = ItemUtils.item(XMaterial.CARROT_ON_A_STICK, Lang.startRewards.toString());
	static ItemStack holoText = ItemUtils.item(XMaterial.PAINTING, Lang.hologramText.toString());
	static ItemStack timerItem = ItemUtils.item(XMaterial.CLOCK, Lang.timer.toString());

	private final StagesGUI stages;

	/* Temporary quest datas */
	private String name = null;
	private NPC startNPC = null;
	private boolean isRepeatable = false;
	private boolean hasScoreboard = true;
	private boolean isHid = false;
	private boolean bypassLimit = false;
	private String endMsg;
	private String hologramText;
	private Dialog dialog;
	private int timer = -1;

	public List<AbstractRequirement> requirements = new ArrayList<>();
	public List<AbstractReward> rewards = new ArrayList<>();
	public List<AbstractReward> rewardsStart = new ArrayList<>();

	/* GUI variables */
	private Inventory inv;
	private Player p;

	private boolean editing = false;
	private boolean stagesEdited = false;
	private Quest edited;
	
	public FinishGUI(StagesGUI gui){
		stages = gui;
	}
	
	public FinishGUI(StagesGUI gui, Quest edited, boolean stagesEdited){
		stages = gui;
		this.edited = edited;
		this.stagesEdited = stagesEdited;
		editing = true;
	}

	public Inventory open(Player p){
		this.p = p;
		if (inv == null){
			String invName = Lang.INVENTORY_DETAILS.toString();
			if (editing){
				invName = invName + " ID: " + edited.getID();
				if (NMS.getMCVersion() <= 8 && invName.length() > 32) invName = Lang.INVENTORY_DETAILS.toString(); // 32 characters limit in 1.8
			}
			inv = Bukkit.createInventory(null, 27, invName);

			inv.setItem(0, multiple.clone());
			inv.setItem(1, scoreboard.clone());
			inv.setItem(2, hide.clone());
			inv.setItem(3, bypass.clone());
			inv.setItem(4, endMessage);
			inv.setItem(5, startDialog);
			// timer at 9
			inv.setItem(11, editRequirements.clone());
			inv.setItem(12, StagesGUI.ending.clone());
			inv.setItem(14, startRewards.clone());
			inv.setItem(16, holoText);

			inv.setItem(7, questStarterSelect.clone());
			inv.setItem(8, questStarterCreate);
			inv.setItem(17, questName.clone());
			inv.setItem(23, create.clone());

			inv.setItem(21, ItemUtils.itemLaterPage());
			
			if (editing) setFromQuest();
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public CustomInventory openLastInv(Player p){
		p.openInventory(inv);
		return this;
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		switch(slot){

		case 0:
			if (isRepeatable = ItemUtils.toggle(current)){
				inv.setItem(9, timerItem);
			}else inv.setItem(9, null);
			break;

		case 1:
			hasScoreboard = ItemUtils.toggle(current);
			break;

		case 2:
			isHid = ItemUtils.toggle(current);
			break;

		case 3:
			bypassLimit = ItemUtils.toggle(current);
			break;

		case 4: // 						End message
			Lang.END_MESSAGE.send(p);
			new TextEditor(p, (obj) -> {
				endMsg = (String) obj;
				openLastInv(p);
			}, () -> {
				openLastInv(p);
			}, () -> {
				endMsg = null;
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 5: // Start dialog
			Utils.sendMessage(p, Lang.NPC_TEXT.toString());
			Editor.enterOrLeave(p, new DialogEditor(p, null, (obj) -> {
				openLastInv(p);
				dialog = (Dialog) obj;
			}, dialog != null ? dialog : new Dialog(null)));
			break;

		case 7: //						Select start NPC
			Editor.enterOrLeave(p, new SelectNPC(p, (obj) -> {
				if (obj != null){
					NPC npc = (NPC) obj;
					startNPC = npc;
					ItemUtils.lore(current, startNPC.getFullName());
				}
				refreshFinish(inv);
				openLastInv(p);
			}));
			break;

		case 8:	//						Create start NPC
			NPCGUI tmp = (NPCGUI) Inventories.create(p, new NPCGUI());
			tmp.run = (obj) -> {
				if (obj != null) {
					startNPC = (NPC) obj;
					startNPC.spawn(p.getLocation().setDirection(new Vector(0, 0, 0)));
					ItemUtils.lore(inv.getItem(7), startNPC.getFullName());
				}
				refreshFinish(inv);
				Inventories.put(p, openLastInv(p), inv);
			};
			break;

		case 9: //						Timer
			Lang.TIMER.send(p);
			new TextEditor(p, (obj) -> {
				timer = (int) obj;
				openLastInv(p);
			}, new NumberParser(Integer.class, true), () -> {
				openLastInv(p);
			}, () -> {
				timer = -1;
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 11:	//						Quest requirements
			Inventories.create(p, new RequirementsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				requirements = (List<AbstractRequirement>) obj;
				ItemUtils.lore(current, Lang.requirements.format(requirements.size()));
			}, requirements));
			break;

		case 12: //						End Rewards
			Inventories.create(p, new RewardsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				rewards = (List<AbstractReward>) obj;
				ItemUtils.lore(current, Lang.rewards.format(rewards.size()));
			}, rewards));
			break;

		case 14: //						Start Rewards
			Inventories.create(p, new RewardsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				rewardsStart = (List<AbstractReward>) obj;
				ItemUtils.lore(current, Lang.rewards.format(rewardsStart.size()));
			}, rewardsStart));
			break;

		case 16: //						Hologram Text
			Lang.HOLOGRAM_TEXT.send(p);
			new TextEditor(p, (txt) -> {
				hologramText = (String) txt;
				openLastInv(p);
			}, () -> {
				openLastInv(p);
			}, () -> {
				hologramText = null;
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 17: //						Quest name
			Lang.QUEST_NAME.send(p);
			new TextEditor(p, (obj) -> {
				name = (String) obj;
				Inventories.put(p, openLastInv(p), inv);
				ItemUtils.name(current, name);
				refreshFinish(inv);
			}, () -> {
					Inventories.put(p, openLastInv(p), inv);
			}, () -> {
					name = null;
					Inventories.put(p, openLastInv(p), inv);
					ItemUtils.name(current, Lang.questName.toString());
					refreshFinish(inv);
			}).enterOrLeave(p);
			break;

		case 21: // 						Later page
			Inventories.create(p, stages);
			break;

		case 23: //						Finish
			if (current.getType() == Material.GOLD_INGOT){
				finish(inv);
			}
			break;

		}
		return true;
	}

	private void refreshFinish(Inventory inv) {
		ItemStack item = inv.getItem(23);
		if (name != null && startNPC != null) {
			item.setType(Material.GOLD_INGOT);
			ItemUtils.name(item, ItemUtils.getName(item).replace("§5", "§6"));
		}else{
			item.setType(XMaterial.NETHER_BRICK.parseMaterial());
			ItemUtils.name(item, ItemUtils.getName(item).replace("§6", "§5"));
		}
	}

	private void finish(Inventory inv){
		Quest qu;
		Map<PlayerAccount, Integer> players = null;
		if (editing){
			if (!stagesEdited) players = new HashMap<>(edited.getStageManager().getPlayersStage());
			edited.remove(false);
			qu = new Quest(name, startNPC, edited.getID());
			qu.copyFinished(edited);
		}else {
			qu = new Quest(name, startNPC, ++BeautyQuests.lastID);
		}
		qu.setRepeatable(isRepeatable);
		qu.setScoreboardEnabled(hasScoreboard);
		qu.setHid(isHid);
		for (LineData ln : stages.getLinesDatas()){
			try{
				StageType type = (StageType) ln.get("type");
				AbstractStage stage = StageCreator.getCreators().get(type).runnables.finish(ln, qu);
				stage.setRewards((List<AbstractReward>) ln.get("rewards"));
				stage.setCustomText((String) ln.get("customText"));
				stage.setStartMessage((String) ln.get("startMessage"));
				qu.getStageManager().addStage(stage);
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(p, " lineToStage");
				ex.printStackTrace();
				continue;
			}
		}
		if (editing && !stagesEdited) qu.getStageManager().setPlayersStage(players);
		qu.setBypassLimit(bypassLimit);
		qu.setTimer(timer);
		qu.setRewards(rewards);
		qu.setStartRewards(rewardsStart);
		qu.setEndMessage(endMsg);
		qu.setHologramText(hologramText);
		if (dialog != null){
			dialog.setNPC(startNPC);
			qu.setStartDialog(dialog);
		}
		qu.getRequirements().addAll(requirements);

		QuestCreateEvent event = new QuestCreateEvent(p, qu, editing);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			qu.remove(false);
			Utils.sendMessage(p, Lang.CANCELLED.toString());
		}else {
			BeautyQuests.getInstance().addQuest(qu);
			Utils.sendMessage(p, ((!editing) ? Lang.SUCCESFULLY_CREATED : Lang.SUCCESFULLY_EDITED).toString(), qu.getName(), qu.getStageManager().getStageSize());
			BeautyQuests.logger.info("New quest created: " + qu.getName() + ", ID " + qu.getID() + ", by " + p.getName());
			if (editing) BeautyQuests.getInstance().getLogger().info("Quest " + qu.getName() + " has been edited");
		}
		Inventories.closeAndExit(p);
	}

	private void setFromQuest(){
		name = edited.getName();
		ItemUtils.name(inv.getItem(17), name);
		rewards = edited.getRewards();
		ItemUtils.lore(inv.getItem(12), Lang.rewards.format(rewards.size()));
		rewardsStart = edited.getStartRewards();
		ItemUtils.lore(inv.getItem(14), Lang.rewards.format(rewardsStart.size()));
		startNPC = edited.getStarter();
		ItemUtils.lore(inv.getItem(7), startNPC.getFullName());
		hologramText = edited.getRawHologramText();
		isRepeatable = ItemUtils.set(inv.getItem(0), edited.isRepeatable());
		timer = edited.getRawTimer();
		if (isRepeatable) inv.setItem(9, timerItem);
		hasScoreboard = ItemUtils.set(inv.getItem(1), edited.isScoreboardEnabled());
		isHid = ItemUtils.set(inv.getItem(2), edited.isHid());
		bypassLimit = ItemUtils.set(inv.getItem(3), edited.canBypassLimit());
		endMsg = edited.getEndMessage();
		dialog = edited.getStartDialog();
		inv.setItem(23, edit.clone());
		requirements = new ArrayList<>(edited.getRequirements());
		ItemUtils.lore(inv.getItem(11), Lang.requirements.format(requirements.size()));
		refreshFinish(inv);
	}

}