package fr.skytasul.quests.gui.creation;

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
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.QuestCreateEvent;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.options.UpdatableOptionSet;
import fr.skytasul.quests.api.options.UpdatableOptionSet.Updatable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.options.OptionName;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import fr.skytasul.quests.structure.QuestBranchImplementation;
import fr.skytasul.quests.structure.StageControllerImplementation;

public class FinishGUI extends UpdatableOptionSet<Updatable> implements Gui {

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
				if (MinecraftVersion.MAJOR <= 8 && invName.length() > 32) invName = Lang.INVENTORY_DETAILS.toString(); // 32 characters limit in 1.8
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

	public Gui reopen(Player p){
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
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(
					"Editing quest " + session.getQuestEdited().getID() + " with keep datas: " + keepPlayerDatas);
			session.getQuestEdited().remove(false, false);
			qu = new Quest(session.getQuestEdited().getID(), session.getQuestEdited().getFile());
		}else {
			int id = -1;
			if (session.hasCustomID()) {
				if (QuestsAPI.getAPI().getQuestsManager().getQuests().stream().anyMatch(x -> x.getID() == session.getCustomID())) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot create quest with custom ID " + session.getCustomID() + " because another quest with this ID already exists.");
				}else {
					id = session.getCustomID();
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("A quest will be created with custom ID " + id + ".");
				}
			}
			if (id == -1)
				id = QuestsAPI.getAPI().getQuestsManager().getFreeQuestID();
			qu = new Quest(id);
		}
		
		for (QuestOption<?> option : this) {
			if (option.hasCustomValue()) qu.addOption(option);
		}

		QuestBranchImplementation mainBranch = new QuestBranchImplementation(qu.getBranchesManager());
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
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Players quests datas will be kept for quest #" + qu.getId()
							+ " - this may cause datas issues.");
				} else
					BeautyQuests.getInstance().getPlayersManager().removeQuestDatas(session.getQuestEdited())
							.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded()
									.logError("An error occurred while removing player datas after quest edition", p));
			}

			QuestsAPI.getAPI().getQuestsManager().addQuest(qu);
			Utils.sendMessage(p, ((!session.isEdition()) ? Lang.SUCCESFULLY_CREATED : Lang.SUCCESFULLY_EDITED).toString(), qu.getName(), qu.getBranchesManager().getBranchesAmount());
			Utils.playPluginSound(p, "ENTITY_VILLAGER_YES", 1);
			QuestsPlugin.getPlugin().getLoggerExpanded().info("New quest created: " + qu.getName() + ", ID " + qu.getId() + ", by " + p.getName());
			if (session.isEdition()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().info("Quest " + qu.getName() + " has been edited");
				if (failure) BeautyQuests.getInstance().createQuestBackup(qu.getFile().toPath(), "Error occurred while editing");
			}
			try {
				qu.saveToFile();
			}catch (Exception e) {
				Lang.ERROR_OCCURED.send(p, "initial quest save");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error when trying to save newly created quest.", e);
			}
			
			if (keepPlayerDatas) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					PlayerAccount account = PlayersManager.getPlayerAccount(p);
					if (account == null) continue;
					if (account.hasQuestDatas(qu)) {
						PlayerQuestDatasImplementation datas = account.getQuestDatas(qu);
						datas.questEdited();
						if (datas.getBranch() == -1) continue;
						QuestBranchImplementation branch = qu.getBranchesManager().getBranch(datas.getBranch());
						if (datas.isInEndingStages()) {
							branch.getEndingStages().keySet().forEach(stage -> stage.joins(account, player));
						}else branch.getRegularStage(datas.getStage()).joins(account, p);
					}
				}
			}
			
			QuestsAPI.getAPI().propagateQuestsHandlers(handler -> {
				if (session.isEdition())
					handler.questEdit(qu, session.getQuestEdited(), keepPlayerDatas);
				else handler.questCreate(qu);
			});
		}
		
		Inventories.closeAndExit(p);
	}
	
	private boolean loadBranch(QuestBranchImplementation branch, StagesGUI gui) {
		boolean failure = false;
		for (StageCreation<?> creation : gui.getStageCreations()) {
			try{
				AbstractStage stage = createStage(creation, branch);
				if (creation.isEndingStage()) {
					StagesGUI newGUI = creation.getLeadingBranch();
					QuestBranchImplementation newBranch = null;
					if (!newGUI.isEmpty()){
						newBranch = new QuestBranchImplementation(branch.getBranchesManager());
						branch.getBranchesManager().addBranch(newBranch);
						failure |= loadBranch(newBranch, newGUI);
					}
					branch.addEndStage(stage, newBranch);
				}else branch.addRegularStage(stage);
			}catch (Exception ex) {
				failure = true;
				Lang.ERROR_OCCURED.send(p, " lineToStage");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred wheh creating branch from GUI.", ex);
			}
		}
		return failure;
	}

	public <T extends AbstractStage> T createStage(StageCreation<T> creation, QuestBranchImplementation branch) {
		StageControllerImplementation<T> controller = new StageControllerImplementation<>(branch, creation.getType());
		T stage = creation.finish(controller);
		controller.setStage(stage);
		return stage;
	}

	private void setStagesEdited() {
		keepPlayerDatas = false;
		int resetSlot = QuestOptionCreator.calculateSlot(6);
		inv.setItem(resetSlot, ItemUtils.itemSwitch(Lang.keepDatas.toString(), false, QuestOption.formatDescription(Lang.keepDatasLore.toString())));
		clicks.put(resetSlot, new Item(resetSlot) {
			
			@Override
			public void click(Player p, ItemStack item, ClickType click) {
				keepPlayerDatas = ItemUtils.toggleSwitch(item);
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
	
}