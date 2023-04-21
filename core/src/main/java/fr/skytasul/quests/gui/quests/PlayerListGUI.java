package fr.skytasul.quests.gui.quests;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class PlayerListGUI implements CustomInventory {

	private static final String UNSELECTED_PREFIX = "§7○ ";
	private static final String SELECTED_PREFIX = "§b§l● ";
	
	private Inventory inv;
	private Player open;
	private PlayerAccount acc;
	private boolean hide;
	
	private int page = 0;
	private Category cat = Category.NONE;
	private List<Quest> quests;
	
	public PlayerListGUI(PlayerAccount acc) {
		this(acc, true);
	}
	
	public PlayerListGUI(PlayerAccount acc, boolean hide) {
		this.acc = acc;
		this.hide = hide;
	}
	
	@Override
	public Inventory open(Player p) {
		open = p;
		inv = Bukkit.createInventory(null, 45, Lang.INVENTORY_PLAYER_LIST.format(acc.getOfflinePlayer().getName()));

		setBarItem(0, ItemUtils.itemLaterPage);
		setBarItem(4, ItemUtils.itemNextPage);
		
		for (Category enabledCat : QuestsConfiguration.getMenuConfig().getEnabledTabs()) {
			setBarItem(enabledCat.slot, enabledCat.item);
		}

		if (Category.IN_PROGRESS.isEnabled()) {
			setCategory(Category.IN_PROGRESS);
			if (quests.isEmpty() && QuestsConfiguration.getMenuConfig().isNotStartedTabOpenedWhenEmpty() && Category.NOT_STARTED.isEnabled()) setCategory(Category.NOT_STARTED);
		}else if (Category.NOT_STARTED.isEnabled()) {
			setCategory(Category.NOT_STARTED);
		}else setCategory(Category.FINISHED);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void setQuests(List<Quest> quests) {
		this.quests = quests;
		quests.sort(null);
	}
	
	private void setCategory(Category category){
		if (cat == category) return;
		if (cat != Category.NONE) toggleCategorySelected();
		cat = category;
		page = 0;
		toggleCategorySelected();
		setItems();
		
		DyeColor color = cat == Category.FINISHED ? DyeColor.GREEN: (cat == Category.IN_PROGRESS ? DyeColor.YELLOW : DyeColor.RED);
		for (int i = 0; i < 5; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
	}
	
	private void setItems(){
		for (int i = 0; i < 35; i++) setMainItem(i, null);
		switch (cat){
		
		case FINISHED:
			displayQuests(QuestsAPI.getQuests().getQuestsFinished(acc, hide), qu -> {
				List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getQuestDescription(), qu, acc, cat, Source.MENU).formatDescription();
				if (QuestsConfiguration.getDialogsConfig().isHistoryEnabled() && acc.getQuestDatas(qu).hasFlowDialogs()) {
					if (!lore.isEmpty()) lore.add(null);
					lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
				}
				return createQuestItem(qu, lore);
			});
			break;
		
		case IN_PROGRESS:
			displayQuests(QuestsAPI.getQuests().getQuestsStarted(acc, true, false), qu -> {
				List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getQuestDescription(), qu, acc, cat, Source.MENU).formatDescription();
				
				boolean hasDialogs = QuestsConfiguration.getDialogsConfig().isHistoryEnabled() && acc.getQuestDatas(qu).hasFlowDialogs();
				boolean cancellable = QuestsConfiguration.getMenuConfig().allowPlayerCancelQuest() && qu.isCancellable();
				if (cancellable || hasDialogs) {
					if (!lore.isEmpty()) lore.add(null);
					if (cancellable) lore.add("§8" + Lang.ClickLeft + " §8> " + Lang.cancelLore);
					if (hasDialogs) lore.add("§8" + Lang.ClickRight + " §8> " + Lang.dialogsHistoryLore);
				}
				return createQuestItem(qu, lore);
			});
			break;
			
		case NOT_STARTED:
			displayQuests(QuestsAPI.getQuests().getQuestsNotStarted(acc, hide, true).stream().filter(quest -> !quest.isHiddenWhenRequirementsNotMet() || quest.isLauncheable(acc.getPlayer(), acc, false)).collect(Collectors.toList()), qu -> {
				return createQuestItem(qu, new QuestDescriptionContext(QuestsConfiguration.getQuestDescription(), qu, acc, cat, Source.MENU).formatDescription());
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
				item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + qu.getID());
				BeautyQuests.logger.severe("An error ocurred when creating item of quest " + qu.getID() + " for account " + acc.abstractAcc.getIdentifier(), ex);
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
		return ItemUtils.nameAndLore(qu.getQuestItem().clone(), open.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu.getName(), qu.getID()) : Lang.formatNormal.format(qu.getName()), lore);
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
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		PlayerListGUI thiz = this;
		switch (slot % 9){
		case 8:
			int barSlot = (slot - 8) / 9;
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
				Category category = Category.values()[barSlot];
				if (category.isEnabled()) setCategory(category);
				break;
				
			}
			break;
			
		case 7:
			break;
			
		default:
			int id = (int) (slot - (Math.floor(slot * 1D / 9D) * 2) + page * 35);
			Quest qu = quests.get(id);
			if (cat == Category.NOT_STARTED) {
				if (!qu.getOptionValueOrDef(OptionStartable.class)) break;
				if (!acc.isCurrent()) break;
				Player target = acc.getPlayer();
				if (qu.isLauncheable(target, acc, true)) {
					p.closeInventory();
					qu.attemptStart(target);
				}
			}else {
				if (click.isRightClick()) {
					if (QuestsConfiguration.getDialogsConfig().isHistoryEnabled() && acc.getQuestDatas(qu).hasFlowDialogs()) {
						Utils.playPluginSound(p, "ITEM_BOOK_PAGE_TURN", 0.5f, 1.4f);
						new DialogHistoryGUI(acc, qu, () -> {
							Inventories.put(p, thiz, inv);
							p.openInventory(inv);
						}).create(p);
					}
				}else if (click.isLeftClick()) {
					if (QuestsConfiguration.getMenuConfig().allowPlayerCancelQuest() && cat == Category.IN_PROGRESS && qu.isCancellable()) {
						Inventories.create(p, new ConfirmGUI(() -> {
							qu.cancelPlayer(acc);
						}, () -> {
							p.openInventory(inv);
							Inventories.put(p, thiz, inv);
						}, Lang.INDICATION_CANCEL.format(qu.getName())));
					}
				}
			}
			break;
			
		}
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

	public enum Category {
		NONE,
		FINISHED(
				1,
				XMaterial.WRITTEN_BOOK,
				Lang.finisheds.toString()),
		IN_PROGRESS(
				2,
				XMaterial.BOOK,
				Lang.inProgress.toString()),
		NOT_STARTED(
				3,
				XMaterial.WRITABLE_BOOK,
				Lang.notStarteds.toString());

		private int slot;
		private ItemStack item;
		
		private Category() {}
		
		private Category(int slot, XMaterial material, String name) {
			this.slot = slot;
			this.item = ItemUtils.item(material, UNSELECTED_PREFIX + name);
		}
		
		public boolean isEnabled() {
			return QuestsConfiguration.getMenuConfig().getEnabledTabs().contains(this);
		}
		
		public static Category fromString(String name) {
			try {
				Category cat = Category.valueOf(name.toUpperCase());
				if (cat != NONE) return cat;
			}catch (IllegalArgumentException ex) {}
			return null;
		}
		
	}
	
}
