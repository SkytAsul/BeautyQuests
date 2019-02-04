package fr.skytasul.quests.gui.creation;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ItemCreatorGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class ItemsGUI implements CustomInventory{
	
	private ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.itemsNone.toString());
	
	public List<ItemStack> items;
	
	public Inventory inv;
	private RunnableObj run;
	
	private boolean cancel = true;
	
	private boolean closeNoDelete = false;
	
	public ItemsGUI(RunnableObj run) {
		this.run = run;
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_ITEMS.toString());
		
		inv.setItem(8, ItemUtils.itemDone());
		for (int i = 0; i < 8; i++) inv.setItem(i, none);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void setItemsFromRew(List<ItemStack> list){
		this.items = list;
		int id = 0;
		for (int slot = 0; slot < 8; slot++){
			if (items.size() == id) break;
			ItemStack stack = items.get(id);
			if (stack.getAmount() <= 64) {
				inv.setItem(slot, stack);
				id++;
			}else {
				stack.setAmount(stack.getAmount() - 64);
				stack = stack.clone();
				stack.setAmount(64);
				inv.setItem(slot, stack);
			}
		}
	}

	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		if (slot == 8) return true;
		if (none.equals(current)){
			inv.setItem(slot, cursor);
			return true;
		}
		return false;
	}
	
	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			closeNoDelete = true;
			items.clear();
			for (int i = 0; i < 8; i++){
				ItemStack is = inv.getItem(i);
				if (is != null){
					if (!is.equals(none)){
						addItem(inv.getItem(i));
					}
				}
			}
			run.run(null);
		}else {
			if (current.equals(none)){
				Inventories.create(p, new ItemCreatorGUI((obj) -> {
						if (obj != null) inv.setItem(slot, (ItemStack) obj);
						Inventories.put(p, openLastInv(p), inv);
				}));
			}else {
				cancel = false;
				new BukkitRunnable() {
					public void run() {
						inv.setItem(slot, none);
					}
				}.runTaskLaterAsynchronously(BeautyQuests.getInstance(), 1L);
			}
		}
	}
	
	private void addItem(ItemStack add){
		for (ItemStack exist : items){
			if (exist.isSimilar(add)){
				int maxAdding = exist.getMaxStackSize() - exist.getAmount();
				if (maxAdding < exist.getAmount()){
					exist.setAmount(exist.getMaxStackSize());
					add.setAmount(add.getAmount() - maxAdding);
				}else {
					exist.setAmount(exist.getAmount() + add.getAmount());
					return;
				}
			}
		}
		items.add(add);
	}
	
	public boolean cancelClick(){
		boolean tmp = cancel;
		cancel = true;
		return tmp;
	}

	public boolean onClose(Player p, Inventory inv){
		return !closeNoDelete;
	}

}