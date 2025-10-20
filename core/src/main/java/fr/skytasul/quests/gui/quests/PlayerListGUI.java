package fr.skytasul.quests.gui.quests;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.scoreboards.Scoreboard;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerListGUI extends PagedGUI<Quest> {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.PlayerListGUI");

	static final String UNSELECTED_PREFIX = "§7○ ";
	private static final String SELECTED_PREFIX = "§b§l● ";

	private final @NotNull Player player;
	private final boolean showHidden;
	private final @NotNull Collection<? extends Quester> questers;

	private @Nullable Map<Quest, Quester> quests;
	private @Nullable PlayerListCategory cat = null;

	public PlayerListGUI(@NotNull QuesterManager questerManager, @NotNull Player player, boolean showHidden) {
		super(Lang.INVENTORY_PLAYER_LIST.quickFormat("player_name", player.getName()), DyeColor.GRAY,
				Collections.emptyList());

		this.player = player;
		this.showHidden = showHidden;
		this.questers = questerManager.getPlayerQuesters(player);
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		setObjects(Collections.emptyList());
		super.populate(player, inventory);

		for (PlayerListCategory enabledCat : QuestsConfiguration.getConfig().getQuestsMenuConfig().getEnabledTabs()) {
			setBarItem(enabledCat.getSlot(),
					ItemUtils.name(enabledCat.getIcon(), UNSELECTED_PREFIX + enabledCat.getName()));
		}

		if (PlayerListCategory.IN_PROGRESS.isEnabled()) {
			setCategory(PlayerListCategory.IN_PROGRESS);
			if (objects.isEmpty() && QuestsConfiguration.getConfig().getQuestsMenuConfig().isNotStartedTabOpenedWhenEmpty()
					&& PlayerListCategory.NOT_STARTED.isEnabled())
				setCategory(PlayerListCategory.NOT_STARTED);
		}else if (PlayerListCategory.NOT_STARTED.isEnabled()) {
			setCategory(PlayerListCategory.NOT_STARTED);
		}else setCategory(PlayerListCategory.FINISHED);
	}

	private void setCategory(PlayerListCategory category){
		if (cat == category)
			return;
		if (cat != null)
			setCategorySelected(false);
		cat = category;
		setCategorySelected(true);

		setSeparatorColor(cat.getColor());

		quests = new HashMap<>();
		for (var quester : questers) {
			List<? extends Quest> questerQuests = switch (cat) {
				case FINISHED -> QuestsAPI.getAPI().getQuestsManager().getQuestsFinished(quester, !showHidden);
				case IN_PROGRESS -> QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(quester, true, false);
				case NOT_STARTED -> QuestsAPI.getAPI().getQuestsManager().getQuestsNotStarted(quester, !showHidden, true)
						.stream()
						.filter(quest -> !quest.isHiddenWhenRequirementsNotMet() || quest.canStart(player, false))
						.collect(Collectors.toList());
				default -> throw new UnsupportedOperationException();
			};
			for (var quest : questerQuests)
				quests.put(quest, quester);
		}

		setObjects(quests.keySet().stream().sorted().toList());
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull Quest qu) {
		var quester = quests.get(qu);

		ItemStack item;
		boolean glittering = false;

		try {
			List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
					qu, player, quester, cat, DescriptionSource.MENU).formatDescription();
			switch (cat) {
				case FINISHED:
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled() && hadDialog(qu, quester)) {
						if (!lore.isEmpty())
							lore.add(null);
						lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
					}
					break;

				case IN_PROGRESS:
					var additionalLore = new ArrayList<String>();
					if (QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest() && qu.isCancellable())
						additionalLore.add("§8" + Lang.ClickLeft + " §8> " + Lang.cancelLore);
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled() && hadDialog(qu, quester))
						additionalLore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
					if (BeautyQuests.getInstance().getScoreboardManager() != null && qu.isScoreboardEnabled()) {
						var scoreboard = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(player);
						if (scoreboard != null) {
							boolean isPinned = scoreboard.getEntry(qu, quester).map(entry -> entry.isPinned()).orElse(false);
							additionalLore.add("§8" + Lang.ClickShiftLeft + " §8> "
									+ (isPinned ? Lang.scoreboardUnpinLore : Lang.scoreboardPinLore));
							if (isPinned)
								glittering = true;
						}
					}
					if (!additionalLore.isEmpty() && !lore.isEmpty())
						lore.add(null);
					lore.addAll(additionalLore);
					break;

				case NOT_STARTED:
					break;

				default:
					throw new UnsupportedOperationException();

			}
			item = ItemUtils.nameAndLore(qu.getQuestItem().clone(),
					player.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu) : Lang.formatNormal.format(qu),
					lore);
			ItemUtils.setGlittering(item, glittering);
		} catch (Exception ex) {
			item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + qu.getId());
			LOGGER.severe("An error ocurred when creating item of quest {} for {}", ex,
					qu.getId(), quester.getDetailedName());
		}
		return item;
	}

	@Override
	protected void barClick(GuiClickEvent event, int barSlot) {
		Optional<PlayerListCategory> clickedCat =
				Arrays.stream(PlayerListCategory.values()).filter(cat -> cat.getSlot() == barSlot).findAny();
		if (clickedCat.isPresent()) {
			if (clickedCat.get().isEnabled())
				setCategory(clickedCat.get());
		} else
			super.barClick(event, barSlot);
	}

	@Override
	public void click(@NotNull Quest qu, @NotNull ItemStack item, @NotNull ClickType clickType) {
		var quester = quests.get(qu);

		if (cat == PlayerListCategory.NOT_STARTED) {
			if (!qu.getOptionValueOrDef(OptionStartable.class))
				return;
			if (!quester.isActive())
				return;
			if (qu.canStart(player, true)) {
				close();
				qu.attemptStart(player).whenComplete(LOGGER.logError(result -> {
					if (result && QuestsConfiguration.getConfig().getQuestsMenuConfig().keepMenuOpen()) {
						reopen(getViewer(), true);
					}
				}, "An error occurred while giving a quest to {}", null, player.getName()));
			}
		} else {
			switch (clickType) {
				case LEFT:
					if (QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest()
							&& cat == PlayerListCategory.IN_PROGRESS && qu.isCancellable()) {
						QuestsPlugin.getPlugin().getGuiManager().getFactory()
								.createConfirmation(() -> {
									qu.cancelQuester(quester);
									if (QuestsConfiguration.getConfig().getQuestsMenuConfig().keepMenuOpen()) {
										reopen(getViewer(), true);
									}
								}, this::reopen, Lang.INDICATION_CANCEL.format(qu)).open(player);
					}
					break;
				case RIGHT:
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled() && hadDialog(qu, quester)) {
						QuestUtils.playPluginSound(QuestsPlugin.getPlugin().getAudiences().player(getViewer()),
								"ITEM_BOOK_PAGE_TURN", 0.5f, 1.4f);
						new DialogHistoryGUI(quester, qu, this::reopen).open(getViewer());
					}
					break;
				case SHIFT_LEFT:
					if (cat == PlayerListCategory.IN_PROGRESS && qu.isScoreboardEnabled()
							&& BeautyQuests.getInstance().getScoreboardManager() != null) {
						Scoreboard sb = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(player);
						if (sb != null) {
							sb.getEntry(qu, quester).ifPresent(entry -> {
								entry.setPinned(!entry.isPinned());
								setItems(); // refresh the glittering effect
							});
						}
					}
					break;
				default:
					break;
			}
		}
	}

	private static boolean hadDialog(Quest quest, Quester quester) {
		var data = quester.getDataHolder().getQuestDataIfPresent(quest);
		if (data.isEmpty())
			return false;

		if (!data.get().hasStarted() && !data.get().hasFinishedOnce())
			return false;

		if (quest.hasOption(OptionStartDialog.class))
			return true;

		return !DialogHistoryGUI.getDialogable(data.get(), true).isEmpty();
	}

	private void setCategorySelected(boolean selected) {
		ItemStack is = getInventory().getItem(cat.getSlot() * 9 + 8);
		String name;
		if (selected) {
			ItemUtils.setGlittering(is, true);
			name = SELECTED_PREFIX + cat.getName();
		} else {
			ItemUtils.setGlittering(is, false);
			name = UNSELECTED_PREFIX + cat.getName();
		}
		ItemUtils.name(is, name);
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}

}
