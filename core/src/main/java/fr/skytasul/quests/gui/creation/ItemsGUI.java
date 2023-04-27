package fr.skytasul.quests.gui.creation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.CustomInventory.CloseBehavior;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.misc.ItemCreatorGUI;

public class ItemsGUI implements CustomInventory {
	
	public static ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.itemsNone.toString());
	
	private Consumer<List<ItemStack>> end;
	private Map<Integer, ItemStack> items = new HashMap<>();
	private int size;
	
	public Inventory inv;
	
	public ItemsGUI(Consumer<List<ItemStack>> end, List<ItemStack> itemsList) {
		this.end = end;
		Utils.extractItems(itemsList).forEach(item -> items.put(items.size(), item));
		this.size = (int) (Math.ceil((items.size() + 1D) / 9D) * 9);
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, size, Lang.INVENTORY_ITEMS.toString());
		
		inv.setItem(size - 1, ItemUtils.itemDone);
		
		for (int i = 0; i < size - 1; i++) {
			if (i < items.size()) {
				inv.setItem(i, items.get(i));
			}else inv.setItem(i, none);
		}
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private boolean addItem(Player p, ItemStack item, int slot) {
		items.put(slot, item);
		for (int i = 0; i < size - 1; i++) {
			ItemStack is = inv.getItem(i);
			if (is.equals(none)) return false;
		}
		size += 9;
		Inventories.closeWithoutExit(p);
		Inventories.put(p, this, open(p));
		return true;
	}

	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		if (slot == size - 1) return true;
		if (none.equals(current)){
			inv.setItem(slot, cursor);
			Utils.runSync(() -> {
				player.setItemOnCursor(null);
				addItem(player, cursor, slot);
			});
			return true;
		}else Utils.runSync(() -> items.put(slot, inv.getItem(slot)));
		return false;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == size - 1) {
			Inventories.closeAndExit(p);
			end.accept(items.values().stream().filter(x -> x != null).collect(Collectors.toList()));
		}else {
			if (current.equals(none)){
				new ItemCreatorGUI(item -> {
					if (item != null) inv.setItem(slot, item);
					if (!addItem(p, item, slot)) {
						Inventories.put(p, this, inv);
						p.openInventory(inv);
					}
				}, true).open(p);
			}else {
				if (click.isLeftClick() || (click.isRightClick() && current.getAmount() == 1)) {
					Utils.runSync(() -> {
						inv.setItem(slot, none);
						items.remove(slot);
					});
				}else Utils.runSync(() -> items.put(slot, inv.getItem(slot)));
				return false;
			}
		}
		return true;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return StandardCloseBehavior.REOPEN;
	}

}