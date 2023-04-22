package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.QuestCreateEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.options.UpdatableOptionSet;
import fr.skytasul.quests.api.options.UpdatableOptionSet.Updatable;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.nms.NMS;

public class FinishGUI extends UpdatableOptionSet<Updatable> implements CustomInventory {

	private final QuestCreationSession session;

	/* Temporary quest datas */
	private Map<Integer, Item> clicks = new HashMap<>();

	/* GUI variables */
	public Inventory inv;
	private Player p;

	private Boolean keepPlayerDatas = null;
	
	private UpdatableItem done;
	
	public FinishGUI(QuestCreationSession session) {
		this.session = session;
	}

	@Override
	public Inventory open(Player p){
		this.p = p;
		if (inv == null){
			String invName = Lang.INVENTORY_DETAILS.toString();
			if (session.isEdition()) {
				invName = invName + " #" + session.getQuestEdited().getID();
				if (NMS.getMCVersion() <= 8 && invName.length() > 32) invName = Lang.INVENTORY_DETAILS.toString(); // 32 characters limit in 1.8
			}
			inv = Bukkit.createInventory(null, (int) Math.ceil((QuestOptionCreator.creators.values().stream().mapToInt(creator -> creator.slot).max().getAsInt() + 1) / 9D) * 9, invName);
			
			for (QuestOptionCreator<?, ?> creator : QuestOptionCreator.creators.values()) {
				QuestOption<?> option;
				if (session.isEdition() && session.getQuestEdited().hasOption(creator.optionClass)) {
					option = session.getQuestEdited().getOption(creator.optionClass).clone();
				}else {
					option = creator.optionSupplier.get();
				}
				UpdatableItem item = new UpdatableItem(creator.slot) {
					
					@Override
					public void click(Player p, ItemStack item, ClickType click) {
						option.click(FinishGUI.this, p, item, slot, click);
					}
					
					@Override
					public boolean clickCursor(Player p, ItemStack item, ItemStack cursor) {
						return option.clickCursor(FinishGUI.this, p, item, cursor, slot);
					}
					
					@Override
					public void update() {
						if (option.shouldDisplay(FinishGUI.this)) {
							inv.setItem(slot, option.getItemStack(FinishGUI.this));
						}else inv.setItem(slot, null);
						option.updatedDependencies(FinishGUI.this, inv.getItem(slot));
					}
				};
				addOption(option, item);
				clicks.put(creator.slot, item);
			}
			super.calculateDependencies();
			
			for (QuestOption<?> option : this) {
				if (option.shouldDisplay(this)) inv.setItem(option.getOptionCreator().slot, option.getItemStack(this));
			}
			
			int pageSlot = QuestOptionCreator.calculateSlot(3);
			clicks.put(pageSlot, new Item(pageSlot) {
				@Override
				public void click(Player p, ItemStack item, ClickType click) {
					session.openMainGUI(p);
				}
			});
			inv.setItem(pageSlot, ItemUtils.itemLaterPage);

			done = new UpdatableItem(QuestOptionCreator.calculateSlot(5)) {
				@Override
				public void update() {
					boolean enabled = getOption(OptionName.class).getValue() != null;
					XMaterial type = enabled ? XMaterial.GOLD_INGOT : XMaterial.NETHER_BRICK;
					String itemName = (enabled ? ChatColor.GOLD : ChatColor.DARK_PURPLE).toString() + (session.isEdition() ? Lang.edit : Lang.create).toString();
					String itemLore = QuestOption.formatDescription(Lang.createLore.toString()) + (enabled ? " §a✔" : " §c✖");
					String[] lore = keepPlayerDatas == null || keepPlayerDatas.booleanValue() ? new String[] { itemLore } : new String[] { itemLore, "", Lang.resetLore.toString() };
					
					ItemStack item = inv.getItem(slot);
					
					if (item == null) {
						inv.setItem(slot, ItemUtils.item(type, itemName, lore));
						return;
					}else if (!type.isSimilar(item)) {
						type.setType(item);
						ItemUtils.name(item, itemName);
					}
					ItemUtils.lore(item, lore);
				}
				
				@Override
				public void click(Player p, ItemStack item, ClickType click) {
					if (getOption(OptionName.class).getValue() != null) finish();
				}
			};
			
			done.update();
			clicks.put(done.slot, done);
			getWrapper(OptionName.class).dependent.add(done);
			
		}
		if (session.areStagesEdited() && keepPlayerDatas == null) setStagesEdited();

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public CustomInventory reopen(Player p){
		Inventories.put(p, this, inv);
		p.openInventory(inv);
		return this;
	}

	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		clicks.get(slot).click(p, current, click);
		return true;
	}
	
	@Override
	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot) {
		return clicks.get(slot).clickCursor(p, current, cursor);
	}

	private void finish(){
		boolean keepPlayerDatas = Boolean.TRUE.equals(this.keepPlayerDatas);
		Quest qu;
		if (session.isEdition()) {
			DebugUtils.logMessage(
					"Editing quest " + session.getQuestEdited().getID() + " with keep datas: " + keepPlayerDatas);
			session.getQuestEdited().remove(false, false);
			qu = new Quest(session.getQuestEdited().getID(), session.getQuestEdited().getFile());
		}else {
			int id = -1;
			if (session.hasCustomID()) {
				if (QuestsAPI.getQuests().getQuests().stream().anyMatch(x -> x.getID() == session.getCustomID())) {
					BeautyQuests.logger.warning("Cannot create quest with custom ID " + session.getCustomID() + " because another quest with this ID already exists.");
				}else {
					id = session.getCustomID();
					BeautyQuests.logger.warning("A quest will be created with custom ID " + id + ".");
				}
			}
			if (id == -1)
				id = QuestsAPI.getQuests().getFreeQuestID();
			qu = new Quest(id);
		}
		
		for (QuestOption<?> option : this) {
			if (option.hasCustomValue()) qu.addOption(option);
		}

		QuestBranch mainBranch = new QuestBranch(qu.getBranchesManager());
		qu.getBranchesManager().addBranch(mainBranch);
		boolean failure = loadBranch(mainBranch, session.getMainGUI());

		QuestCreateEvent event = new QuestCreateEvent(p, qu, session.isEdition());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			qu.remove(false, true);
			Utils.sendMessage(p, Lang.CANCELLED.toString());
		}else {

			if (session.areStagesEdited()) {
				if (keepPlayerDatas) {
					BeautyQuests.logger.warning("Players quests datas will be kept for quest #" + qu.getID()
							+ " - this may cause datas issues.");
				} else
					BeautyQuests.getInstance().getPlayersManager().removeQuestDatas(session.getQuestEdited())
							.whenComplete(BeautyQuests.logger
									.logError("An error occurred while removing player datas after quest edition", p));
			}

			QuestsAPI.getQuests().addQuest(qu);
			Utils.sendMessage(p, ((!session.isEdition()) ? Lang.SUCCESFULLY_CREATED : Lang.SUCCESFULLY_EDITED).toString(), qu.getName(), qu.getBranchesManager().getBranchesAmount());
			Utils.playPluginSound(p, "ENTITY_VILLAGER_YES", 1);
			BeautyQuests.logger.info("New quest created: " + qu.getName() + ", ID " + qu.getID() + ", by " + p.getName());
			if (session.isEdition()) {
				BeautyQuests.getInstance().getLogger().info("Quest " + qu.getName() + " has been edited");
				if (failure) BeautyQuests.getInstance().createQuestBackup(qu.getFile().toPath(), "Error occurred while editing");
			}
			try {
				qu.saveToFile();
			}catch (Exception e) {
				Lang.ERROR_OCCURED.send(p, "initial quest save");
				BeautyQuests.logger.severe("Error when trying to save newly created quest.", e);
			}
			
			if (keepPlayerDatas) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					PlayerAccount account = PlayersManager.getPlayerAccount(p);
					if (account == null) continue;
					if (account.hasQuestDatas(qu)) {
						PlayerQuestDatas datas = account.getQuestDatas(qu);
						datas.questEdited();
						if (datas.getBranch() == -1) continue;
						QuestBranch branch = qu.getBranchesManager().getBranch(datas.getBranch());
						if (datas.isInEndingStages()) {
							branch.getEndingStages().keySet().forEach(stage -> stage.joins(account, p));
						}else branch.getRegularStage(datas.getStage()).joins(account, p);
					}
				}
			}
			
			QuestsAPI.propagateQuestsHandlers(handler -> {
				if (session.isEdition())
					handler.questEdit(qu, session.getQuestEdited(), keepPlayerDatas);
				else handler.questCreate(qu);
			});
		}
		
		Inventories.closeAndExit(p);
	}
	
	private boolean loadBranch(QuestBranch branch, StagesGUI gui) {
		boolean failure = false;
		for (StageCreation<?> creation : gui.getStageCreations()) {
			try{
				AbstractStage stage = creation.finish(branch);
				if (creation.isEndingStage()) {
					StagesGUI newGUI = creation.getLeadingBranch();
					QuestBranch newBranch = null;
					if (!newGUI.isEmpty()){
						newBranch = new QuestBranch(branch.getBranchesManager());
						branch.getBranchesManager().addBranch(newBranch);
						failure |= loadBranch(newBranch, newGUI);
					}
					branch.addEndStage(stage, newBranch);
				}else branch.addRegularStage(stage);
			}catch (Exception ex) {
				failure = true;
				Lang.ERROR_OCCURED.send(p, " lineToStage");
				BeautyQuests.logger.severe("An error occurred wheh creating branch from GUI.", ex);
			}
		}
		return failure;
	}

	private void setStagesEdited() {
		keepPlayerDatas = false;
		int resetSlot = QuestOptionCreator.calculateSlot(6);
		inv.setItem(resetSlot, ItemUtils.itemSwitch(Lang.keepDatas.toString(), false, QuestOption.formatDescription(Lang.keepDatasLore.toString())));
		clicks.put(resetSlot, new Item(resetSlot) {
			
			@Override
			public void click(Player p, ItemStack item, ClickType click) {
				keepPlayerDatas = ItemUtils.toggle(item);
				done.update();
			}
			
		});
		done.update();
	}
	
	abstract class Item {
		protected final int slot;
		
		protected Item(int slot) {
			this.slot = slot;
		}
		
		public abstract void click(Player p, ItemStack item, ClickType click);
		
		public boolean clickCursor(Player p, ItemStack item, ItemStack cursor) {
			return true;
		}
	}
	
	abstract class UpdatableItem extends Item implements Updatable {
		protected UpdatableItem(int slot) {
			super(slot);
		}
	}

	public static void initialize(){
		DebugUtils.logMessage("Initlializing default quest options.");
		
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("pool", 9, OptionQuestPool.class, OptionQuestPool::new, null));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("name", 10, OptionName.class, OptionName::new, null));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("description", 12, OptionDescription.class, OptionDescription::new, null));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("customItem", 13, OptionQuestItem.class, OptionQuestItem::new, QuestsConfiguration.getItemMaterial(), "customMaterial"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("confirmMessage", 15, OptionConfirmMessage.class, OptionConfirmMessage::new, null));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("hologramText", 17, OptionHologramText.class, OptionHologramText::new, Lang.HologramText.toString()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("bypassLimit", 18, OptionBypassLimit.class, OptionBypassLimit::new, false));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("startableFromGUI", 19, OptionStartable.class, OptionStartable::new, false));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("failOnDeath", 20, OptionFailOnDeath.class, OptionFailOnDeath::new, false));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("cancellable", 21, OptionCancellable.class, OptionCancellable::new, true));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("cancelActions", 22, OptionCancelRewards.class, OptionCancelRewards::new, new ArrayList<>()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("hologramLaunch", 25, OptionHologramLaunch.class, OptionHologramLaunch::new, QuestsConfiguration.getHoloLaunchItem()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("hologramLaunchNo", 26, OptionHologramLaunchNo.class, OptionHologramLaunchNo::new, QuestsConfiguration.getHoloLaunchNoItem()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("scoreboard", 27, OptionScoreboardEnabled.class, OptionScoreboardEnabled::new, true));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("hideNoRequirements", 28, OptionHideNoRequirements.class, OptionHideNoRequirements::new, false));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("auto", 29, OptionAutoQuest.class, OptionAutoQuest::new, false));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("repeatable", 30, OptionRepeatable.class, OptionRepeatable::new, false, "multiple"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("timer", 31, OptionTimer.class, OptionTimer::new, QuestsConfiguration.getTimeBetween()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("visibility", 32, OptionVisibility.class, OptionVisibility::new, Arrays.asList(OptionVisibility.VisibilityLocation.values()), "hid", "hide"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("endSound", 34, OptionEndSound.class, OptionEndSound::new, QuestsConfiguration.getFinishSound()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("firework", 35, OptionFirework.class, OptionFirework::new, QuestsConfiguration.getDefaultFirework()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("requirements", 36, OptionRequirements.class, OptionRequirements::new, new ArrayList<>()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("startRewards", 38, OptionStartRewards.class, OptionStartRewards::new, new ArrayList<>(), "startRewardsList"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("startMessage", 39, OptionStartMessage.class, OptionStartMessage::new, QuestsConfiguration.getPrefix() + Lang.STARTED_QUEST.toString()));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("starterNPC", 40, OptionStarterNPC.class, OptionStarterNPC::new, null, "starterID"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("startDialog", 41, OptionStartDialog.class, OptionStartDialog::new, null));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("endRewards", 43, OptionEndRewards.class, OptionEndRewards::new, new ArrayList<>(), "rewardsList"));
		QuestsAPI.registerQuestOption(new QuestOptionCreator<>("endMsg", 44, OptionEndMessage.class, OptionEndMessage::new, QuestsConfiguration.getPrefix() + Lang.FINISHED_BASE.toString()));
	}
	
}