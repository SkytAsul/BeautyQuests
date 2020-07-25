package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;
import java.util.List;

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
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class PlayerListGUI implements CustomInventory {

	private Inventory inv;
	private Player open;
	private PlayerAccount acc;
	
	private int page = 0;
	private Category cat = Category.NONE;
	private List<Quest> quests;
	
	public PlayerListGUI(PlayerAccount acc){
		this.acc = acc;
	}
	
	public Inventory open(Player p) {
		open = p;
		inv = Bukkit.createInventory(null, 45, Lang.INVENTORY_PLAYER_LIST.format(acc.getOfflinePlayer().getName()));

		setBarItem(0, ItemUtils.itemLaterPage);
		setBarItem(4, ItemUtils.itemNextPage);
		
		setBarItem(1, ItemUtils.item(XMaterial.WRITTEN_BOOK, "§7" + Lang.finisheds.toString()));
		setBarItem(2, ItemUtils.item(XMaterial.BOOK, "§7" + Lang.inProgress.toString()));
		setBarItem(3, ItemUtils.item(XMaterial.WRITABLE_BOOK, "§7" + Lang.notStarteds.toString()));
		
		setCategory(Category.IN_PROGRESS);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void setCategory(Category category){
		if (cat == category) return;
		if (cat != Category.NONE) toggleCategoryEnchanted();
		cat = category;
		page = 0;
		toggleCategoryEnchanted();
		setItems();
		
		DyeColor color = cat == Category.FINISHED ? DyeColor.GREEN: (cat == Category.IN_PROGRESS ? DyeColor.YELLOW : DyeColor.RED);
		for (int i = 0; i < 5; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
	}
	
	private void setItems(){
		for (int i = 0; i < 35; i++) setMainItem(i, null);
		switch (cat){
		
		case FINISHED:
			quests = QuestsAPI.getQuestsFinished(acc);
			for (int i = page * 35; i < quests.size(); i++){
				if (i == (page + 1) * 35) break;
				Quest qu = quests.get(i);
				String[] lore = null;
				if (qu.isRepeatable()){
					int timeLeft = qu.getTimeLeft(acc);
					if (timeLeft == 0){
						lore = new String[]{Lang.canRedo.toString()};
					}else if (timeLeft > 0) {
						lore = new String[]{Lang.timeWait.format(timeLeft)};
					}
				}
				setMainItem(i - page * 35, createQuestItem(qu, lore));
			}
			break;
		
		case IN_PROGRESS:
			quests = QuestsAPI.getQuestsStarteds(acc);
			for (int i = page * 35; i < quests.size(); i++){
				if (i == (page + 1) * 35) break;
				Quest qu = quests.get(i);
				ItemStack item;
				try {
					List<String> desc = new ArrayList<>(Utils.wordWrap(qu.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.MENU), 45));
					if (QuestsConfiguration.allowPlayerCancelQuest() && qu.isCancellable()) {
						desc.add(null);
						desc.add(Lang.cancelLore.toString());
					}
					item = createQuestItem(qu, desc.toArray(new String[0]));
				}catch (Exception ex) {
					item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + qu.getID());
					BeautyQuests.getInstance().getLogger().severe("An error ocurred when creating item of quest " + qu.getID() + " for account " + acc.abstractAcc.getIdentifier());
					ex.printStackTrace();
				}
				setMainItem(i - page * 35, item);
			}
			break;
			
		case NOT_STARTED:
			quests = QuestsAPI.getQuestsUnstarted(acc, true);
			for (int i = page * 35; i < quests.size(); i++){
				if (i == (page + 1) * 35) break;
				Quest qu = quests.get(i);
				setMainItem(i - page * 35, createQuestItem(qu, qu.getDescription()));
			}
			break;

		default:
			break;
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
	
	private ItemStack createQuestItem(Quest qu, String... lore){
		return ItemUtils.item(qu.getQuestMaterial(), open.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu.getName(), qu.getID()) : Lang.formatNormal.format(qu.getName()), lore);
	}
	
	private void toggleCategoryEnchanted(){
		ItemStack is = inv.getItem(cat.ordinal() * 9 + 8);
		ItemMeta im = is.getItemMeta();
		String name = im.getDisplayName();
		if (!im.hasEnchant(Enchantment.DURABILITY)) {
			im.addEnchant(Enchantment.DURABILITY, 0, true);
			name = "§b§l" + name.substring(2);
		}else{
			im.removeEnchant(Enchantment.DURABILITY);
			name = "§7" + name.substring(4);
		}
		im.setDisplayName(name);
		is.setItemMeta(im);
	}

	
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
				setCategory(Category.values()[barSlot]);
				break;
				
			}
			break;
			
		case 7:
			break;
			
		default:
			if (QuestsConfiguration.allowPlayerCancelQuest() && cat == Category.IN_PROGRESS) {
				int id = (int) (slot - (Math.floor(slot * 1D / 9D)*2) + page*35);
				Quest qu = quests.get(id);
				if (!qu.isCancellable()) break;
				Inventories.create(p, new ConfirmGUI(() -> {
					qu.cancelPlayer(acc);
				}, () -> {
					p.openInventory(inv);
					Inventories.put(p, thiz, inv);
				}, Lang.INDICATION_CANCEL.format(qu.getName())));
			}
			break;
			
		}
		return true;
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

	enum Category{
		NONE, FINISHED, IN_PROGRESS, NOT_STARTED;
	}
	
}
