package fr.skytasul.quests.gui.quests;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.utils.QuestUtils;

public class PlayerListGUI extends AbstractGui {

	static final String UNSELECTED_PREFIX = "§7○ ";
	private static final String SELECTED_PREFIX = "§b§l● ";
	
	private Inventory inv;
	private Player open;
	private PlayerAccountImplementation acc;
	private boolean hide;
	
	private int page = 0;
	private @Nullable PlayerListCategory cat = null;
	private List<Quest> quests;
	
	public PlayerListGUI(PlayerAccountImplementation acc) {
		this(acc, true);
	}
	
	public PlayerListGUI(PlayerAccountImplementation acc, boolean hide) {
		this.acc = acc;
		this.hide = hide;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 45, Lang.INVENTORY_PLAYER_LIST.format(acc));
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		setBarItem(0, ItemUtils.itemLaterPage);
		setBarItem(4, ItemUtils.itemNextPage);
		
		for (PlayerListCategory enabledCat : QuestsConfiguration.getConfig().getQuestsMenuConfig().getEnabledTabs()) {
			setBarItem(enabledCat.getSlot(),
					ItemUtils.item(enabledCat.getMaterial(), UNSELECTED_PREFIX + enabledCat.getName()));
		}

		if (PlayerListCategory.IN_PROGRESS.isEnabled()) {
			setCategory(PlayerListCategory.IN_PROGRESS);
			if (quests.isEmpty() && QuestsConfiguration.getConfig().getQuestsMenuConfig().isNotStartedTabOpenedWhenEmpty()
					&& PlayerListCategory.NOT_STARTED.isEnabled())
				setCategory(PlayerListCategory.NOT_STARTED);
		}else if (PlayerListCategory.NOT_STARTED.isEnabled()) {
			setCategory(PlayerListCategory.NOT_STARTED);
		}else setCategory(PlayerListCategory.FINISHED);
	}
	
	private void setQuests(List<Quest> quests) {
		this.quests = quests;
		quests.sort(null);
	}
	
	private void setCategory(PlayerListCategory category){
		if (cat == category) return;
		if (cat != null)
			toggleCategorySelected();
		cat = category;
		page = 0;
		toggleCategorySelected();
		setItems();
		
		DyeColor color = cat == PlayerListCategory.FINISHED ? DyeColor.GREEN: (cat == PlayerListCategory.IN_PROGRESS ? DyeColor.YELLOW : DyeColor.RED);
		for (int i = 0; i < 5; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
	}
	
	private void setItems(){
		for (int i = 0; i < 35; i++) setMainItem(i, null);
		switch (cat){
		
		case FINISHED:
			displayQuests(QuestsAPI.getAPI().getQuestsManager().getQuestsFinished(acc, hide), qu -> {
				List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
						qu, acc, cat, DescriptionSource.MENU).formatDescription();
				if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
						&& acc.getQuestDatas(qu).hasFlowDialogs()) {
					if (!lore.isEmpty()) lore.add(null);
					lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
				}
				return createQuestItem(qu, lore);
			});
			break;
		
		case IN_PROGRESS:
			displayQuests(QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(acc, true, false), qu -> {
				List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
						qu, acc, cat, DescriptionSource.MENU).formatDescription();
				
				boolean hasDialogs = QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
						&& acc.getQuestDatas(qu).hasFlowDialogs();
				boolean cancellable =
						QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest() && qu.isCancellable();
				if (cancellable || hasDialogs) {
					if (!lore.isEmpty()) lore.add(null);
					if (cancellable) lore.add("§8" + Lang.ClickLeft + " §8> " + Lang.cancelLore);
					if (hasDialogs) lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
				}
				return createQuestItem(qu, lore);
			});
			break;
			
		case NOT_STARTED:
			displayQuests(QuestsAPI.getAPI().getQuestsManager().getQuestsNotStarted(acc, hide, true).stream()
					.filter(quest -> !quest.isHiddenWhenRequirementsNotMet() || quest.canStart(acc.getPlayer(), false))
					.collect(Collectors.toList()), qu -> {
						return createQuestItem(qu,
								new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(), qu,
										acc, cat, DescriptionSource.MENU).formatDescription());
			});
			break;

		default:
			break;
		}
	}
	
	private void displayQuests(List<Quest> quests, Function<Quest, ItemStack> itemProvider) {
		setQuests(quests);
		for (int i = page * 35; i < quests.size(); i++) {
			if (i == (page + 1) * 35) break;
			Quest qu = quests.get(i);
			ItemStack item;
			try {
				item = itemProvider.apply(qu);
			}catch (Exception ex) {
				item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + qu.getId());
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error ocurred when creating item of quest " + qu.getId() + " for account " + acc.abstractAcc.getIdentifier(), ex);
			}
			setMainItem(i - page * 35, item);
		}
	}
	
	private int setMainItem(int mainSlot, ItemStack is){
		int line = (int) Math.floor(mainSlot * 1.0 / 7.0);
		int slot = mainSlot + (2 * line);
		inv.setItem(slot, is);
		return slot;
	}
	
	private int setBarItem(int barSlot, ItemStack is){
		int slot = barSlot * 9 + 8;
		inv.setItem(slot, is);
		return slot;
	}
	
	private ItemStack createQuestItem(Quest qu, List<String> lore) {
		return ItemUtils.nameAndLore(qu.getQuestItem().clone(),
				open.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu) : Lang.formatNormal.format(qu), lore);
	}
	
	private void toggleCategorySelected() {
		ItemStack is = inv.getItem(cat.ordinal() * 9 + 8);
		ItemMeta im = is.getItemMeta();
		String name = im.getDisplayName();
		if (!im.hasEnchant(Enchantment.DURABILITY)) {
			im.addEnchant(Enchantment.DURABILITY, 0, true);
			name = SELECTED_PREFIX + name.substring(UNSELECTED_PREFIX.length());
		}else{
			im.removeEnchant(Enchantment.DURABILITY);
			name = UNSELECTED_PREFIX + name.substring(SELECTED_PREFIX.length());
		}
		im.setDisplayName(name);
		is.setItemMeta(im);
	}

	
	@Override
	public void onClick(GuiClickEvent event) {
		switch (event.getSlot() % 9) {
		case 8:
				int barSlot = (event.getSlot() - 8) / 9;
			switch (barSlot){
			case 0:
				if (page == 0) break;
				page--;
				setItems();
				break;
			case 4:
				page++;
				setItems();
				break;
				
			case 1:
			case 2:
			case 3:
				PlayerListCategory category = PlayerListCategory.values()[barSlot];
				if (category.isEnabled()) setCategory(category);
				break;
				
			}
			break;
			
		case 7:
			break;
			
		default:
			int id = (int) (event.getSlot() - (Math.floor(event.getSlot() * 1D / 9D) * 2) + page * 35);
			Quest qu = quests.get(id);
			if (cat == PlayerListCategory.NOT_STARTED) {
				if (!qu.getOptionValueOrDef(OptionStartable.class)) break;
				if (!acc.isCurrent()) break;
				Player target = acc.getPlayer();
				if (qu.canStart(target, true)) {
					event.close();
					qu.attemptStart(target);
				}
			}else {
				if (event.getClick().isRightClick()) {
					if (QuestsConfiguration.getConfig().getDialogsConfig().isHistoryEnabled()
							&& acc.getQuestDatas(qu).hasFlowDialogs()) {
						QuestUtils.playPluginSound(event.getPlayer(), "ITEM_BOOK_PAGE_TURN", 0.5f, 1.4f);
						new DialogHistoryGUI(acc, qu, event::reopen).open(event.getPlayer());
					}
				} else if (event.getClick().isLeftClick()) {
					if (QuestsConfiguration.getConfig().getQuestsMenuConfig().allowPlayerCancelQuest()
							&& cat == PlayerListCategory.IN_PROGRESS && qu.isCancellable()) {
						QuestsPlugin.getPlugin().getGuiManager().getFactory()
								.createConfirmation(() -> qu.cancelPlayer(acc), event::reopen,
										Lang.INDICATION_CANCEL.format(qu))
								.open(event.getPlayer());
					}
				}
			}
			break;
			
		}
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}
	
}
