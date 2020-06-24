package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
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
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.misc.ItemCreatorGUI;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.npc.NPC;

public class FinishGUI implements CustomInventory{

	static ItemStack multiple = ItemUtils.itemSwitch(Lang.multiple.toString(), false, Lang.multipleLore.toString());
	static ItemStack cancellable = ItemUtils.itemSwitch(Lang.cancellable.toString(), true);
	static ItemStack scoreboard = ItemUtils.itemSwitch(Lang.scoreboard.toString(), true);
	static ItemStack hide = ItemUtils.itemSwitch(Lang.hide.toString(), false);
	static ItemStack bypass = ItemUtils.itemSwitch(Lang.bypass.toString(), false);
	static ItemStack questName = ItemUtils.item(XMaterial.NAME_TAG, Lang.questName.toString());
	static ItemStack questStarterCreate = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.questStarterCreate.toString());
	static ItemStack questStarterSelect = ItemUtils.item(XMaterial.STICK, Lang.questStarterSelect.toString());
	static ItemStack editRequirements = ItemUtils.item(XMaterial.ACACIA_DOOR, Lang.editRequirements.toString());
	static ItemStack endMessage = ItemUtils.item(XMaterial.PAPER, Lang.endMessage.toString());
	static ItemStack startDialog = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.startDialog.toString());
	static ItemStack startRewards = ItemUtils.item(XMaterial.CARROT_ON_A_STICK, Lang.startRewards.toString());
	static ItemStack holoText = ItemUtils.item(XMaterial.PAINTING, Lang.hologramText.toString());
	static ItemStack timerItem = ItemUtils.item(XMaterial.CLOCK, Lang.timer.toString());
	static ItemStack hologramLaunch = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, Lang.hologramLaunch.toString());
	static ItemStack hologramLaunchNo = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, Lang.hologramLaunchNo.toString());
	static ItemStack customConfirmMessage = ItemUtils.item(XMaterial.FEATHER, Lang.customConfirmMessage.toString());
	static ItemStack customDesc = ItemUtils.item(XMaterial.OAK_SIGN, Lang.customDescription.toString());
	static ItemStack customMaterial = ItemUtils.item(QuestsConfiguration.getItemMaterial(), Lang.customMaterial.toString());

	private final StagesGUI stages;

	/* Temporary quest datas */
	private String name = null;
	private NPC startNPC = null;
	private boolean isRepeatable = false;
	private boolean isCancellable = true;
	private boolean hasScoreboard = true;
	private boolean isHid = false;
	private boolean bypassLimit = false;
	private String endMsg;
	private String hologramText;
	private String confirmMessage;
	private String description;
	private Dialog dialog;
	private int timer = -1;
	private XMaterial material;

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
			inv = Bukkit.createInventory(null, 36, invName);

			inv.setItem(0, multiple.clone());
			inv.setItem(1, scoreboard.clone());
			inv.setItem(2, hide.clone());
			inv.setItem(4, endMessage);
			inv.setItem(5, startDialog);
			inv.setItem(7, questStarterSelect.clone());
			inv.setItem(8, questStarterCreate);
			// timer at 9
			inv.setItem(10, bypass.clone());
			if (QuestsConfiguration.allowPlayerCancelQuest()) inv.setItem(11, cancellable.clone());
			inv.setItem(12, editRequirements.clone());
			inv.setItem(13, StagesGUI.ending.clone());
			inv.setItem(14, startRewards.clone());
			inv.setItem(16, holoText);
			inv.setItem(17, questName.clone());

			inv.setItem(20, customMaterial.clone());

			inv.setItem(21, customDesc.clone());
			inv.setItem(22, customConfirmMessage.clone());
			
			inv.setItem(24, hologramLaunch.clone());
			inv.setItem(25, hologramLaunchNo.clone());
			
			inv.setItem(30, ItemUtils.itemLaterPage);
			
			if (editing) setFromQuest();

			refreshFinish(inv);
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
				inv.setItem(9, ItemUtils.lore(timerItem.clone()));
				setTimerItem();
			}else inv.setItem(9, null);
			break;

		case 1:
			hasScoreboard = ItemUtils.toggle(current);
			break;

		case 2:
			isHid = ItemUtils.toggle(current);
			break;

		case 4: // 						End message
			Lang.END_MESSAGE.send(p);
			new TextEditor(p, (obj) -> {
				endMsg = (String) obj;
				ItemUtils.lore(current, endMsg);
				openLastInv(p);
			}, () -> {
				openLastInv(p);
			}, () -> {
				endMsg = null;
				ItemUtils.lore(current);
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 5: // Start dialog
			Utils.sendMessage(p, Lang.NPC_TEXT.toString());
			Editor.enterOrLeave(p, new DialogEditor(p, (obj) -> {
				openLastInv(p);
				dialog = obj;
			}, dialog != null ? dialog : new Dialog(null)));
			break;

		case 7: //						Select start NPC
			Editor.enterOrLeave(p, new SelectNPC(p, (obj) -> {
				if (obj != null){
					startNPC = obj;
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
					startNPC = obj;
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
				setTimerItem();
				openLastInv(p);
			}, new NumberParser(Integer.class, true), () -> {
				openLastInv(p);
			}, () -> {
				timer = -1;
				setTimerItem();
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 10:
			bypassLimit = ItemUtils.toggle(current);
			break;

		case 11:
			isCancellable = ItemUtils.toggle(current);
			break;

		case 12:	//						Quest requirements
			Inventories.create(p, new RequirementsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				requirements = obj;
				ItemUtils.lore(current, Lang.requirements.format(requirements.size()));
			}, requirements));
			break;

		case 13: //						End Rewards
			Inventories.create(p, new RewardsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				rewards = obj;
				ItemUtils.lore(current, Lang.rewards.format(rewards.size()));
			}, rewards));
			break;

		case 14: //						Start Rewards
			Inventories.create(p, new RewardsGUI((obj) -> {
				Inventories.put(p, openLastInv(p), inv);
				rewardsStart = obj;
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
			
		case 20: //						Custom Material
			Lang.QUEST_MATERIAL.send(p);
			new TextEditor(p, (obj) -> {
				material = (XMaterial) obj;
				current.setType(material.parseMaterial());
				openLastInv(p);
			}, new MaterialParser(false), () -> openLastInv(p), () -> {
				material = null;
				current.setType(QuestsConfiguration.getItemMaterial().parseMaterial());
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 21: //						Custom Description
			Lang.QUEST_DESCRIPTION.send(p);
			new TextEditor(p, (obj) -> {
				description = (String) obj;
				openLastInv(p);
				ItemUtils.lore(current, description);
			}, () -> openLastInv(p), () -> {
				description = null;
				ItemUtils.lore(current);
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 22: //						Custom Confirm Message
			Lang.CONFIRM_MESSAGE.send(p);
			new TextEditor(p, (obj) -> {
				confirmMessage = (String) obj;
				openLastInv(p);
				ItemUtils.lore(current, confirmMessage);
			}, () -> openLastInv(p), () -> {
				confirmMessage = null;
				ItemUtils.lore(current);
				openLastInv(p);
			}).enterOrLeave(p);
			break;
			
		case 24:
		case 25:
			new ItemCreatorGUI((item) -> {
				if (item != null) inv.setItem(slot, item);
				Inventories.put(p, openLastInv(p), inv);
			}, true);
			break;

		case 30: // 						Later page
			Inventories.create(p, stages);
			break;

		case 32: //						Finish
			if (current.getType() == Material.GOLD_INGOT){
				finish();
			}
			break;

		}
		return true;
	}
	
	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		if (slot == 24 || slot == 25){
			if (current.equals(slot == 24 ? hologramLaunch : hologramLaunchNo)){
				inv.setItem(slot, cursor);
				return true;
			}
			return false;
		}
		return true;
	}

	private void setTimerItem() {
		ItemUtils.lore(inv.getItem(9), timer == -1 ? "Default timer" : timer + " minutes");
	}

	private void refreshFinish(Inventory inv) {
		ItemStack item = inv.getItem(32);
		if (item == null) {
			item = ItemUtils.item(XMaterial.NETHER_BRICK, ChatColor.DARK_PURPLE.toString() + (editing ? Lang.edit : Lang.create), Lang.createLore.toString());
			if (stagesEdited) ItemUtils.loreAdd(item, Lang.resetLore.toString());
			inv.setItem(32, item);
		}
		if (name != null && startNPC != null && item.getType() != Material.GOLD_INGOT) {
			item.setType(Material.GOLD_INGOT);
			ItemUtils.name(item, ItemUtils.getName(item).replace("§5", "§6"));
		}else if (item.getType() != Material.NETHER_BRICK) {
			item.setType(XMaterial.NETHER_BRICK.parseMaterial());
			ItemUtils.name(item, ItemUtils.getName(item).replace("§6", "§5"));
		}
	}

	private void finish(){
		Quest qu;
		if (editing){
			edited.remove(false);
			qu = new Quest(name, startNPC, edited.getID());
		}else {
			qu = new Quest(name, startNPC, ++BeautyQuests.lastID);
		}
		qu.setRepeatable(isRepeatable);
		qu.setScoreboardEnabled(hasScoreboard);
		qu.setHid(isHid);
		qu.setBypassLimit(bypassLimit);
		qu.setCancellable(isCancellable);
		qu.setTimer(timer);
		qu.setRewards(rewards);
		qu.setStartRewards(rewardsStart);
		qu.setRequirements(requirements);
		qu.setEndMessage(endMsg);
		qu.setHologramText(hologramText);
		qu.setCustomConfirmMessage(confirmMessage);
		qu.setCustomDescription(description);
		qu.setCustomMaterial(material);
		if (!hologramLaunch.equals(inv.getItem(24))) qu.setHologramLaunch(inv.getItem(24));
		if (!hologramLaunchNo.equals(inv.getItem(25))) qu.setHologramLaunchNo(inv.getItem(25));
		qu.getRequirements().addAll(requirements);
		if (dialog != null){
			dialog.setNPC(startNPC);
			qu.setStartDialog(dialog);
		}
		
		if (stagesEdited) PlayersManager.manager.removeQuestDatas(qu);

		QuestBranch mainBranch = new QuestBranch(qu.getBranchesManager());
		qu.getBranchesManager().addBranch(mainBranch);
		loadBranch(mainBranch, stages);

		QuestCreateEvent event = new QuestCreateEvent(p, qu, editing);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			qu.remove(false);
			Utils.sendMessage(p, Lang.CANCELLED.toString());
		}else {
			BeautyQuests.getInstance().addQuest(qu);
			Utils.sendMessage(p, ((!editing) ? Lang.SUCCESFULLY_CREATED : Lang.SUCCESFULLY_EDITED).toString(), qu.getName(), qu.getBranchesManager().getBranchesAmount());
			BeautyQuests.logger.info("New quest created: " + qu.getName() + ", ID " + qu.getID() + ", by " + p.getName());
			if (editing) BeautyQuests.getInstance().getLogger().info("Quest " + qu.getName() + " has been edited");
			try {
				qu.saveToFile();
			}catch (Exception e) {
				Lang.ERROR_OCCURED.send(p, "initial quest save");
				BeautyQuests.logger.severe("Error when trying to save quest");
				e.printStackTrace();
			}
		}
		Inventories.closeAndExit(p);
	}
	
	private void loadBranch(QuestBranch branch, StagesGUI gui){
		for (LineData ln : gui.getLinesDatas()){
			try{
				StageType type = (StageType) ln.get("type");
				AbstractStage stage = StageCreator.getCreator(type).runnables.finish(ln, branch);
				stage.setRewards((List<AbstractReward>) ln.get("rewards"));
				stage.setCustomText((String) ln.get("customText"));
				stage.setStartMessage((String) ln.get("startMessage"));
				if (ln.containsKey("branch")){
					StagesGUI newGUI = (StagesGUI) ln.get("branch");
					QuestBranch newBranch = null;
					if (!newGUI.isEmpty()){
						newBranch = new QuestBranch(branch.getBranchesManager());
						branch.getBranchesManager().addBranch(newBranch);
						loadBranch(newBranch, newGUI);
					}
					branch.addEndStage(stage, newBranch);
				}else branch.addRegularStage(stage);
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(p, " lineToStage");
				ex.printStackTrace();
				continue;
			}
		}
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
		hologramText = edited.getCustomHologramText();
		confirmMessage = edited.getCustomConfirmMessage();
		if (confirmMessage != null) ItemUtils.lore(inv.getItem(22), confirmMessage);
		description = edited.getCustomDescription();
		if (description != null) ItemUtils.lore(inv.getItem(21), description);
		isRepeatable = ItemUtils.set(inv.getItem(0), edited.isRepeatable());
		timer = edited.getRawTimer();
		if (isRepeatable) {
			inv.setItem(9, timerItem);
			setTimerItem();
		}
		hasScoreboard = ItemUtils.set(inv.getItem(1), edited.isScoreboardEnabled());
		isHid = ItemUtils.set(inv.getItem(2), edited.isHid());
		bypassLimit = ItemUtils.set(inv.getItem(10), edited.canBypassLimit());
		isCancellable = ItemUtils.set(inv.getItem(11), edited.isCancellable());
		endMsg = edited.getEndMessage();
		ItemUtils.lore(inv.getItem(4), endMsg);
		dialog = edited.getStartDialog();
		requirements = edited.getRequirements();
		ItemUtils.lore(inv.getItem(12), Lang.requirements.format(requirements.size()));
		inv.setItem(24, edited.getCustomHologramLaunch());
		inv.setItem(25, edited.getCustomHologramLaunchNo());
		material = edited.getCustomMaterial();
	}

	public void setStagesEdited() {
		if (stagesEdited) return;
		stagesEdited = true;
		ItemUtils.loreAdd(inv.getItem(32), Lang.resetLore.toString());
	}

}