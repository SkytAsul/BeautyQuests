package fr.skytasul.quests.gui.creation.quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.QuestCreateEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedButton.ItemButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.options.UpdatableOptionSet;
import fr.skytasul.quests.api.quests.creation.QuestCreationGui;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.creation.QuestCreationSession;
import fr.skytasul.quests.gui.creation.stages.StageCreationContextImplementation;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.options.OptionName;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import fr.skytasul.quests.structure.QuestBranchImplementation;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.structure.StageControllerImplementation;
import fr.skytasul.quests.utils.QuestUtils;
import net.md_5.bungee.api.ChatColor;

public class QuestCreationGuiImplementation extends LayoutedGUI implements QuestCreationGui {

	private final QuestCreationSession session;
	private final UpdatableOptionSet options;

	private final int doneButtonSlot;
	
	private Boolean keepPlayerDatas = null;
	
	public QuestCreationGuiImplementation(QuestCreationSession session) {
		super(null, new HashMap<>(), StandardCloseBehavior.CONFIRM);
		// null name because it is computed in #instanciate
		this.session = session;
		this.options = new UpdatableOptionSet();

		for (QuestOptionCreator<?, ?> creator : QuestOptionCreator.creators.values()) {
			QuestOption<?> option;
			if (session.isEdition() && session.getQuestEdited().hasOption(creator.optionClass)) {
				option = session.getQuestEdited().getOption(creator.optionClass).clone();
			} else {
				option = creator.optionSupplier.get();
			}

			ItemButton optionButton = new LayoutedButton.ItemButton() {

				@Override
				public void click(@NotNull LayoutedClickEvent event) {
					option.click(new QuestCreationGuiClickEvent(event.getPlayer(), QuestCreationGuiImplementation.this, event.getClicked(),
							event.getCursor(), event.getSlot(), event.getClick()));
				}

				@Override
				public @Nullable ItemStack getItem() {
					return option.getItemStack(options);
				}

				@Override
				public boolean isValid() {
					return option.shouldDisplay(options);
				}

			};
			buttons.put(creator.slot, optionButton);
			options.addOption(option, () -> {
				option.onDependenciesUpdated(options);
				refresh(optionButton);
			});
		}

		options.calculateDependencies();

		buttons.put(QuestOptionCreator.calculateSlot(3),
				LayoutedButton.create(ItemUtils.itemLaterPage, event -> session.openStagesGUI(event.getPlayer())));
	
		doneButtonSlot = QuestOptionCreator.calculateSlot(5);
		buttons.put(doneButtonSlot, LayoutedButton.create(() -> {
			boolean finishable = isFinishable();
			XMaterial type = finishable ? XMaterial.GOLD_INGOT : XMaterial.NETHER_BRICK;
			String itemName = (finishable ? ChatColor.GOLD : ChatColor.DARK_PURPLE).toString()
					+ (session.isEdition() ? Lang.edit : Lang.create).toString();
			List<String> lore = new ArrayList<>(3);
			lore.add(QuestOption.formatDescription(Lang.createLore.toString()) + (finishable ? " §a✔" : " §c✖"));
			if (Boolean.FALSE.equals(keepPlayerDatas)) {
				lore.add("");
				lore.add(Lang.resetLore.toString());
			}
			return ItemUtils.item(type, itemName, lore);
		}, event -> {
			if (isFinishable())
				finish(event.getPlayer());
		}));
		options.getWrapper(OptionName.class).dependent.add(() -> super.refresh(doneButtonSlot));
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		String invName = Lang.INVENTORY_DETAILS.toString();
		if (session.isEdition()) {
			invName = invName + " #" + session.getQuestEdited().getId();
			if (MinecraftVersion.MAJOR <= 8 && invName.length() > 32)
				invName = Lang.INVENTORY_DETAILS.toString(); // 32 characters limit in 1.8
		}

		return Bukkit.createInventory(null, (int) Math.ceil((QuestOptionCreator.getLastSlot() + 1) / 9D) * 9, invName);
	}

	@Override
	protected void refresh(@NotNull Player player, @NotNull Inventory inventory) {
		super.refresh(player, inventory);
		if (session.areStagesEdited() && keepPlayerDatas == null)
			setStagesEdited();
	}

	private boolean isFinishable() {
		return options.getOption(OptionName.class).getValue() != null;
	}

	@Override
	public @NotNull OptionSet getOptionSet() {
		return options;
	}

	@Override
	public void updateOptionItem(@NotNull QuestOption<?> option) {
		refresh(option.getOptionCreator().slot);
	}

	private void finish(Player p) {
		boolean keepPlayerDatas = Boolean.TRUE.equals(this.keepPlayerDatas);
		QuestImplementation qu;
		if (session.isEdition()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(
					"Editing quest " + session.getQuestEdited().getId() + " with keep datas: " + keepPlayerDatas);
			session.getQuestEdited().delete(true, true);
			qu = new QuestImplementation(session.getQuestEdited().getId(), session.getQuestEdited().getFile());
		}else {
			int id = -1;
			if (session.hasCustomID()) {
				if (QuestsAPI.getAPI().getQuestsManager().getQuests().stream()
						.anyMatch(x -> x.getId() == session.getCustomID())) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot create quest with custom ID " + session.getCustomID() + " because another quest with this ID already exists.");
				}else {
					id = session.getCustomID();
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("A quest will be created with custom ID " + id + ".");
				}
			}
			if (id == -1)
				id = BeautyQuests.getInstance().getQuestsManager().getFreeQuestID();
			qu = new QuestImplementation(id);
		}
		
		for (QuestOption<?> option : options) {
			if (option.hasCustomValue()) qu.addOption(option);
		}

		QuestBranchImplementation mainBranch = new QuestBranchImplementation(qu.getBranchesManager());
		qu.getBranchesManager().addBranch(mainBranch);
		boolean failure = loadBranch(p, mainBranch, session.getStagesGUI());

		QuestCreateEvent event = new QuestCreateEvent(p, qu, session.isEdition());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			qu.delete(true, false);
			Lang.CANCELLED.send(p);
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
			Lang msg = session.isEdition() ? Lang.SUCCESFULLY_EDITED : Lang.SUCCESFULLY_CREATED;
			msg.send(p, qu, PlaceholderRegistry.of("quest_branches", qu.getBranchesManager().getBranches().size()));
			QuestUtils.playPluginSound(p, "ENTITY_VILLAGER_YES", 1);
			QuestsPlugin.getPlugin().getLoggerExpanded().info("New quest created: " + qu.getName() + ", ID " + qu.getId() + ", by " + p.getName());
			if (session.isEdition()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().info("Quest " + qu.getName() + " has been edited");
				if (failure) BeautyQuests.getInstance().createQuestBackup(qu.getFile().toPath(), "Error occurred while editing");
			}
			try {
				qu.saveToFile();
			}catch (Exception e) {
				DefaultErrors.sendGeneric(p, "initial quest save");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error when trying to save newly created quest.", e);
			}
			
			if (keepPlayerDatas)
				keepDatas(qu);
			
			QuestsAPI.getAPI().propagateQuestsHandlers(handler -> {
				if (session.isEdition())
					handler.questEdit(qu, session.getQuestEdited(), keepPlayerDatas);
				else handler.questCreate(qu);
			});
		}
		
		close(p);
	}

	private void keepDatas(QuestImplementation qu) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerAccountImplementation account = BeautyQuests.getInstance().getPlayersManager().getAccount(p);
			if (account != null && account.hasQuestDatas(qu)) {
				PlayerQuestDatasImplementation datas = account.getQuestDatas(qu);
				datas.questEdited();
				if (datas.getBranch() == -1) continue;
				QuestBranchImplementation branch = qu.getBranchesManager().getBranch(datas.getBranch());
				if (datas.isInEndingStages()) {
					branch.getEndingStages().forEach(stage -> stage.getStage().getStage().joined(p));
				} else
					branch.getRegularStage(datas.getStage()).getStage().joined(p);
			}
		}
	}
	
	private boolean loadBranch(Player p, QuestBranchImplementation branch, StagesGUI stagesGui) {
		boolean failure = false;
		for (StageCreationContextImplementation context : stagesGui.getStageCreations()) {
			try{
				StageControllerImplementation stage = createStage(context, branch);
				if (context.isEndingStage()) {
					StagesGUI newGUI = context.getEndingBranch();
					QuestBranchImplementation newBranch = null;
					if (!newGUI.isEmpty()){
						newBranch = new QuestBranchImplementation(branch.getManager());
						branch.getManager().addBranch(newBranch);
						failure |= loadBranch(p, newBranch, newGUI);
					}
					branch.addEndStage(stage, newBranch);
				}else branch.addRegularStage(stage);
			}catch (Exception ex) {
				failure = true;
				DefaultErrors.sendGeneric(p, " lineToStage");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred wheh creating branch from GUI.", ex);
			}
		}
		return failure;
	}

	public <T extends AbstractStage> StageControllerImplementation<T> createStage(StageCreationContext<T> context,
			QuestBranchImplementation branch) {
		StageControllerImplementation<T> controller = new StageControllerImplementation<>(branch, context.getType());
		T stage = context.getCreation().finish(controller);
		controller.setStage(stage);
		return controller;
	}

	private void setStagesEdited() {
		keepPlayerDatas = false;
		refresh(doneButtonSlot);
		int resetSlot = QuestOptionCreator.calculateSlot(6);
		buttons.put(resetSlot, LayoutedButton.createSwitch(() -> keepPlayerDatas, Lang.keepDatas.toString(),
				Arrays.asList(QuestOption.formatDescription(Lang.keepDatasLore.toString())),
				event -> {
					keepPlayerDatas = ItemUtils.toggleSwitch(event.getClicked());
					refresh(doneButtonSlot);
				}));
		refresh(resetSlot);
	}
	
}