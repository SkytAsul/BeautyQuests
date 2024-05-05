package fr.skytasul.quests.gui.quests;

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
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerListGUI extends PagedGUI<Quest> {

	static final String UNSELECTED_PREFIX = "§7○ ";
	private static final String SELECTED_PREFIX = "§b§l● ";

	private PlayerAccountImplementation acc;
	private boolean hide;

	private @Nullable PlayerListCategory cat = null;

	public PlayerListGUI(PlayerAccountImplementation acc) {
		this(acc, true);
	}

	public PlayerListGUI(PlayerAccountImplementation acc, boolean hide) {
		super(Lang.INVENTORY_PLAYER_LIST.format(acc), DyeColor.GRAY, Collections.emptyList());
		this.acc = acc;
		this.hide = hide;
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		super.populate(player, inventory);

		for (PlayerListCategory enabledCat : QuestsConfiguration.getConfig().getQuestsMenuConfig().getEnabledTabs()) {
			setBarItem(enabledCat.getSlot(),
					ItemUtils.item(enabledCat.getMaterial(), UNSELECTED_PREFIX + enabledCat.getName()));
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
		if (cat == category) return;
		if (cat != null)
			toggleCategorySelected();
		cat = category;

		setSeparatorColor(cat.getColor());
		DyeColor color = cat == PlayerListCategory.FINISHED ? DyeColor.GREEN: (cat == PlayerListCategory.IN_PROGRESS ? DyeColor.YELLOW : DyeColor.RED);
		for (int i = 0; i < 5; i++)
			getInventory().setItem(i * 9 + 7, ItemUtils.itemSeparator(color));

		List<Quest> quests;
		switch (cat) {
			case FINISHED:
				quests = QuestsAPI.getAPI().getQuestsManager().getQuestsFinished(acc, hide);
				break;

			case IN_PROGRESS:
				quests = QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(acc, true, false);
				break;

			case NOT_STARTED:
				quests = QuestsAPI.getAPI().getQuestsManager().getQuestsNotStarted(acc, hide, true).stream()
						.filter(quest -> !quest.isHiddenWhenRequirementsNotMet() || quest.canStart(acc.getPlayer(), false))
						.collect(Collectors.toList());
				break;

			default:
				throw new UnsupportedOperationException();
		}
		quests.sort(null);

		setObjects(quests);
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull Quest qu) {
		ItemStack item;
		try {
			List<String> lore;
			switch (cat) {
				case FINISHED:
					lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
							qu, acc, cat, DescriptionSource.MENU).formatDescription();
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
							&& acc.getQuestDatas(qu).hasFlowDialogs()) {
						if (!lore.isEmpty())
							lore.add(null);
						lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
					}
					break;

				case IN_PROGRESS:
					lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
							qu, acc, cat, DescriptionSource.MENU).formatDescription();

					boolean hasDialogs = QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
							&& acc.getQuestDatas(qu).hasFlowDialogs();
					boolean cancellable =
							QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest()
									&& qu.isCancellable();
					if (cancellable || hasDialogs) {
						if (!lore.isEmpty())
							lore.add(null);
						if (cancellable)
							lore.add("§8" + Lang.ClickLeft + " §8> " + Lang.cancelLore);
						if (hasDialogs)
							lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
					}
					break;

				case NOT_STARTED:
					lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(), qu,
							acc, cat, DescriptionSource.MENU).formatDescription();
					break;

				default:
					throw new UnsupportedOperationException();

			}
			item = ItemUtils.nameAndLore(qu.getQuestItem().clone(),
					player.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu) : Lang.formatNormal.format(qu),
					lore);
		} catch (Exception ex) {
			item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + qu.getId());
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error ocurred when creating item of quest " + qu.getId()
					+ " for account " + acc.abstractAcc.getIdentifier(), ex);
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
		if (cat == PlayerListCategory.NOT_STARTED) {
			if (!qu.getOptionValueOrDef(OptionStartable.class))
				return;
			if (!acc.isCurrent())
				return;
			Player target = acc.getPlayer();
			if (qu.canStart(target, true)) {
				close();
				qu.attemptStart(target);
			}
		} else {
			if (clickType.isRightClick()) {
				if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
						&& acc.getQuestDatas(qu).hasFlowDialogs()) {
					QuestUtils.playPluginSound(player, "ITEM_BOOK_PAGE_TURN", 0.5f, 1.4f);
					new DialogHistoryGUI(acc, qu, this::reopen).open(player);
				}
			} else if (clickType.isLeftClick()) {
				if (QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest()
						&& cat == PlayerListCategory.IN_PROGRESS && qu.isCancellable()) {
					QuestsPlugin.getPlugin().getGuiManager().getFactory()
							.createConfirmation(() -> qu.cancelPlayer(acc), this::reopen, Lang.INDICATION_CANCEL.format(qu))
							.open(player);
				}
			}
		}
	}

	private void toggleCategorySelected() {
		ItemStack is = getInventory().getItem(cat.getSlot() * 9 + 8);
		String name = ItemUtils.getName(is);
		if (!ItemUtils.isGlittering(is)) {
			ItemUtils.setGlittering(is, true);
			name = SELECTED_PREFIX + name.substring(UNSELECTED_PREFIX.length());
		}else{
			ItemUtils.setGlittering(is, false);
			name = UNSELECTED_PREFIX + name.substring(SELECTED_PREFIX.length());
		}
		ItemUtils.name(is, name);
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}

}
