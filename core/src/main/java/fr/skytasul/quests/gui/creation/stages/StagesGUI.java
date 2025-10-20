package fr.skytasul.quests.gui.creation.stages;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.branches.EndingStage;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.creation.*;
import fr.skytasul.quests.gui.creation.QuestCreationSession;
import fr.skytasul.quests.structure.QuestBranchImplementation;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StagesGUI extends AbstractGui {

	private static final int SLOT_FINISH = 52;

	private static final ItemStack stageCreate = ItemUtils.item(XMaterial.SLIME_BALL, Lang.stageCreate.toString());
	private static final ItemStack notDone = ItemUtils.lore(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getNotDone().clone(), Lang.cantFinish.toString());

	private List<Line> lines = new ArrayList<>();

	private final QuestCreationSession session;
	private final StagesGUI previousBranch;

	int page;
	private boolean stop = false;

	public StagesGUI(QuestCreationSession session) {
		this(session, null);
	}

	public StagesGUI(QuestCreationSession session, StagesGUI previousBranch) {
		this.session = session;
		this.previousBranch = previousBranch;
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 54, Lang.INVENTORY_STAGES.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inv) {
		page = 0;
		for (int i = 0; i < 20; i++)
			lines.add(new Line(i, i >= 15));
		lines.get(0).setCreationState();
		lines.get(15).setCreationState();

		inv.setItem(45, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getPreviousPage());
		inv.setItem(50, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getNextPage());

		inv.setItem(SLOT_FINISH, isEmpty() ? notDone : QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone());
		inv.setItem(53, previousBranch == null ? QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel()
				: ItemUtils.item(XMaterial.FILLED_MAP, Lang.previousBranch.toString()));
		refresh();

		if (session.isEdition() && this == session.getStagesGUI()) {
			editBranch(session.getQuestEdited().getBranchesManager().getBranch(0));
			inv.setItem(SLOT_FINISH, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone());
		}
	}

	public QuestCreationSession getSession() {
		return session;
	}

	public void reopen() {
		reopen(session.getPlayer());
	}

	private String[] getLineManageLore(int line) {
		return new String[] {
				"§7" + Lang.ClickRight + "/" + Lang.ClickLeft + " > §c" + Lang.stageRemove.toString(),
				line == 0 || line == 15 ? ("§8" + Lang.ClickShiftRight + " > " + Lang.stageUp) : "§7" + Lang.ClickShiftRight + " > §e" + Lang.stageUp,
				line == 14 || line == 19 || !getLine(line + 1).isActive()
						? ("§8" + Lang.ClickShiftLeft + " > " + Lang.stageDown)
						: "§7" + Lang.ClickShiftLeft + " > §e" + Lang.stageDown};
	}

	public Line getLine(int id) {
		for (Line l : lines) {
			if (l.lineId == id)
				return l;
		}
		throw new IllegalArgumentException("Unknown line " + id);
	}

	public boolean isEmpty(){
		if (lines.isEmpty()) return true; // if this StagesGUI has never been opened
		return !getLine(0).isActive() && !getLine(15).isActive();
	}

	public void deleteStageLine(StageGuiLine line) {
		lines.stream().filter(x -> x.lineObj == line).findAny()
				.filter(Line::isActive)
				.ifPresent(Line::remove);
	}

	@Override
	public void onClick(@NotNull GuiClickEvent event) {
		int slot = event.getSlot();
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
				if (isEmpty() && previousBranch == null) {
					QuestUtils.playPluginSound(QuestsPlugin.getPlugin().getAudiences().player(event.getPlayer()),
							"ENTITY_VILLAGER_NO", 0.6f);
				}else {
					session.openCreationGUI(event.getPlayer());
				}
			}else if (slot == 53) {
				if (previousBranch == null){ // main inventory = cancel button
					stop = true;
					event.close();
					if (!isEmpty()) {
						if (!session.isEdition()) {
							Lang.QUEST_CANCEL.send(event.getPlayer());
						} else
							Lang.QUEST_EDIT_CANCEL.send(event.getPlayer());
					}
				}else { // branch inventory = previous branch button
					previousBranch.open(event.getPlayer());
				}
			}
		}else {
			session.setStagesEdited();
			Line line = getLine((slot - slot % 9) / 9 + 5 * page);
			StageGuiClickHandler click = line.lineObj.getClick(line.getLineSlot(slot));
			if (click != null)
				click.onClick(new StageGuiClickEvent(event.getPlayer(), event.getClicked(), event.getClick(), line.context));
		}
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return isEmpty() || stop ? StandardCloseBehavior.REMOVE : StandardCloseBehavior.REOPEN;
	}

	private void refresh() {
		for (int i = 0; i < 3; i++)
			getInventory().setItem(i + 46,
					ItemUtils.item(i == page ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.WHITE_STAINED_GLASS_PANE,
							Lang.regularPage.toString()));
		getInventory().setItem(49,
				ItemUtils.item(page == 3 ? XMaterial.MAGENTA_STAINED_GLASS_PANE : XMaterial.PURPLE_STAINED_GLASS_PANE,
						Lang.branchesPage.toString()));

		lines.forEach(l -> l.lineObj.refresh());
	}

	@SuppressWarnings ("rawtypes")
	public List<StageCreationContextImplementation> getStageCreations() {
		return lines.stream().sorted(Comparator.comparingInt(line -> line.lineId)).filter(line -> line.isActive())
				.map(line -> line.context).collect(Collectors.toList());
	}

	private void editBranch(QuestBranchImplementation branch){
		for (StageController stage : branch.getRegularStages()) {
			getLine(branch.getRegularStageId(stage)).setStageEdition(stage);
		}

		for (EndingStage stage : branch.getEndingStages()) {
			getLine(15 + branch.getEndingStageId(stage.getStage())).setStageEdition(stage.getStage(), stage.getBranch());
		}
	}

	class Line {

		int lineId;
		final boolean ending;
		StageLineImplementation lineObj;
		StageCreationContextImplementation<?> context;

		Line(int lineId, boolean ending) {
			this.lineId = lineId;
			this.ending = ending;
			this.lineObj = new StageLineImplementation(this);
		}

		boolean isActive() {
			return context != null;
		}

		void setCreationState() {
			lineObj.clearItems();
			lineObj.setItem(0, stageCreate.clone(), event -> setSelectionState());
		}

		void setSelectionState() {
			lineObj.clearItems();
			int i = 0;
			for (StageType<?> type : QuestsAPI.getAPI().getStages()) {
				lineObj.setItem(++i, type.getItem(), event -> {
					setStageCreation(type).start(event.getPlayer());
				});
			}
		}

		<T extends AbstractStage> StageCreation<T> setStageCreation(StageType<T> type) {
			lineObj.clearItems();

			context = new StageCreationContextImplementation<>(lineObj, type, ending, StagesGUI.this);
			StageCreation<T> creation = type.getCreationSupplier().supply((@NotNull StageCreationContext<T>) context);
			context.setCreation((StageCreation) creation);
			creation.setupLine(lineObj);

			getInventory().setItem(SLOT_FINISH, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone());

			int maxStages = ending ? 20 : 15;
			ItemStack manageItem = ItemUtils.item(XMaterial.BARRIER, Lang.stageType.format(type), getLineManageLore(lineId));
			lineObj.setItem(0, manageItem, event -> {
				switch (event.getClick()) {
					case LEFT:
					case RIGHT:
						remove();
						break;
					case SHIFT_LEFT:
						descend();
						break;
					case SHIFT_RIGHT:
						ascend();
						break;
					default:
						break;
				}
			});

			if (lineId != maxStages - 1) {
				Line next = getLine(lineId + 1);
				if (!next.isActive())
					next.setCreationState();
			}

			if (ending) {
				if (context.getEndingBranch() == null)
					context.setEndingBranch(new StagesGUI(session, StagesGUI.this));
				lineObj.setItem(14, ItemUtils.item(XMaterial.FILLED_MAP, Lang.newBranch.toString()),
						event -> context.getEndingBranch().open(event.getPlayer()));
			}

			if (lineId != 0 && lineId != 15)
				getLine(lineId - 1).updateLineManageLore();

			return creation;
		}

		void setStageEdition(StageController stage) {
			setStageEdition(stage, null);
		}

		void setStageEdition(StageController stage, @Nullable QuestBranch branch) {
			@SuppressWarnings("rawtypes")
			StageCreation creation = setStageCreation(stage.getStageType());

			if (branch != null) {
				context.getEndingBranch().repopulate(session.getPlayer());
				context.getEndingBranch().editBranch((QuestBranchImplementation) branch);
			}

			creation.edit(stage.getStage());
			lineObj.setPage(0);
		}

		void updateLineManageLore() {
			if (isActive())
				lineObj.refreshItemLore(0, getLineManageLore(lineId));
		}

		boolean isFirst() {
			return lineId == 0 || lineId == 15;
		}

		boolean isLast() {
			return lineId == 14 || lineId == 19;
		}

		void remove() {
			context = null;
			int maxStages = ending ? 20 : 15;
			if (lineId != maxStages - 1) {
				lineObj.clearItems();

				int oldId = lineId;
				Line lastLine = this;
				for (int i = lineId + 1; i < maxStages; i++) {
					Line nextLine = getLine(i);
					nextLine.exchangeLines(lastLine);
					if (!nextLine.isActive()) {
						if (nextLine.lineObj.isEmpty())
							nextLine.setCreationState();
						break;
					}
				}
				if (oldId == 0 || oldId == 15)
					getLine(oldId).updateLineManageLore();
			} else
				setCreationState();
			if (!isFirst())
				getLine(lineId - 1).updateLineManageLore();
			if (isEmpty())
				getInventory().setItem(SLOT_FINISH, notDone);
		}

		void descend() {
			if (!isLast()) {
				Line down = getLine(lineId + 1);
				if (down.isActive()) {
					down.exchangeLines(this);
					updateLineManageLore();
					down.updateLineManageLore();
				}
			}
		}

		void ascend() {
			if (!isFirst()) {
				Line up = getLine(lineId - 1);
				up.exchangeLines(this);
				updateLineManageLore();
				up.updateLineManageLore();
			}
		}

		void exchangeLines(Line other) {
			if (other == null || other == this)
				return;
			int newLine = other.lineId;

			other.lineId = lineId;
			other.lineObj.refresh();

			lineId = newLine;
			lineObj.refresh();
		}

		int getRawSlot(int lineSlot) {
			return lineId * 9 - page * 5 * 9 + lineSlot;
		}

		int getLineSlot(int rawSlot) {
			return rawSlot - (lineId * 9 - page * 5 * 9);
		}

		public boolean isShown() {
			return lineId >= page * 5 && lineId < (page + 1) * 5;
		}

		public void setItem(int lineSlot, ItemStack item) {
			getInventory().setItem(getRawSlot(lineSlot), item);
		}

		public ItemStack getItem(int lineSlot) {
			return getInventory().getItem(getRawSlot(lineSlot));
		}

	}

}