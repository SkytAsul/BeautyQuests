package fr.skytasul.quests.gui.quests;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.CategorizedPagedGUI;
import fr.skytasul.quests.api.gui.templates.PagedGUI.BarButton;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.options.OptionQuestPool;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.scoreboards.Scoreboard;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerListGUI extends CategorizedPagedGUI<Quest> {

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

		if (PlayerListCategory.IN_PROGRESS.isEnabled()) {
			selectCategory(PlayerListCategory.IN_PROGRESS);
			if (quests.isEmpty() && QuestsConfiguration.getConfig().getQuestsMenuConfig().isNotStartedTabOpenedWhenEmpty()
					&& PlayerListCategory.NOT_STARTED.isEnabled())
				selectCategory(PlayerListCategory.NOT_STARTED);
		}else if (PlayerListCategory.NOT_STARTED.isEnabled()) {
			selectCategory(PlayerListCategory.NOT_STARTED);
		} else
			selectCategory(PlayerListCategory.FINISHED);

		for (PlayerListCategory enabledCat : QuestsConfiguration.getConfig().getQuestsMenuConfig().getEnabledTabs()) {
			if (cat != enabledCat)
				setCategoryItem(enabledCat, false);
		}
	}

	@Override
	public void refresh(@NotNull Player player) {
		refreshShownQuests(); // will internally refresh the delegate
	}

	private void selectCategory(PlayerListCategory category) {
		if (cat == category) return;
		if (cat != null)
			setCategoryItem(cat, false);
		cat = category;
		setCategoryItem(cat, true);

		getDelegate().setSeparatorColor(cat.getColor());

		refreshShownQuests();
	}

	private void refreshShownQuests() {
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

		var groupedQuests = new HashMap<QuestPool, Category<Quest>>();
		for (Quest quest : quests.keySet()) {
			var pool = quest.getOptionValueOrDef(OptionQuestPool.class);
			var group = groupedQuests.computeIfAbsent(pool, __ -> {
				String categoryId = pool == null ? "other" : Integer.toString(pool.getId());
				return new Category<>(categoryId, new ArrayList<>(), getCategoryItem(pool));
			});
			group.objects().add(quest);
		}
		setCategories(groupedQuests.values());
	}

	private @NotNull ItemStack getCategoryItem(@Nullable QuestPool pool) {
		if (pool == null)
			return ItemUtils.item(XMaterial.CHEST, "Other quests");
		return ItemUtils.item(XMaterial.CHEST, "Pool #%d".formatted(pool.getId()));
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
					getViewer().hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu) : Lang.formatNormal.format(qu),
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
	public void click(@NotNull Quest qu, @NotNull ItemStack item, @NotNull ClickType clickType) {
		var quester = quests.get(qu);

		if (cat == PlayerListCategory.NOT_STARTED) {
			if (!qu.getOptionValueOrDef(OptionStartable.class))
				return;
			if (!quester.isActive())
				return;
			if (qu.canStart(player, true)) {
				getDelegate().close();
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
								}, () -> reopen(getViewer()), Lang.INDICATION_CANCEL.format(qu)).open(player);
					}
					break;
				case RIGHT:
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled() && hadDialog(qu, quester)) {
						QuestUtils.playPluginSound(QuestsPlugin.getPlugin().getAudiences().player(getViewer()),
								"ITEM_BOOK_PAGE_TURN", 0.5f, 1.4f);
						new DialogHistoryGUI(quester, qu, () -> reopen(getViewer())).open(getViewer());
					}
					break;
				case SHIFT_LEFT:
					if (cat == PlayerListCategory.IN_PROGRESS && qu.isScoreboardEnabled()
							&& BeautyQuests.getInstance().getScoreboardManager() != null) {
						Scoreboard sb = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(player);
						if (sb != null) {
							sb.getEntry(qu, quester).ifPresent(entry -> {
								entry.setPinned(!entry.isPinned());
								getDelegate().setItems(); // refresh the glittering effect
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

	private void setCategoryItem(PlayerListCategory category, boolean selected) {
		var item = category.getIcon().clone();

		if (selected) {
			ItemUtils.setGlittering(item, true);
			ItemUtils.name(item, SELECTED_PREFIX + cat.getName());
		} else {
			ItemUtils.setGlittering(item, false);
			ItemUtils.name(item, UNSELECTED_PREFIX + cat.getName());
		}
		getDelegate().setBarButton(category.getSlot(), new BarButton(item, event -> selectCategory(category)));
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}

}
