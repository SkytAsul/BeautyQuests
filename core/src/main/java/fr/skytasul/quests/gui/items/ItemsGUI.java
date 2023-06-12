package fr.skytasul.quests.gui.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.utils.QuestUtils;

public class ItemsGUI extends AbstractGui {
	
	public static ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.itemsNone.toString());
	
	private Consumer<List<ItemStack>> end;
	private Map<Integer, ItemStack> items = new HashMap<>();
	private int size;
	
	public ItemsGUI(Consumer<List<ItemStack>> end, List<ItemStack> itemsList) {
		this.end = end;
		Utils.extractItems(itemsList).forEach(item -> items.put(items.size(), item));
		this.size = (int) (Math.ceil((items.size() + 1D) / 9D) * 9);
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, size, Lang.INVENTORY_ITEMS.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inv) {
		inv.setItem(size - 1, ItemUtils.itemDone);
		
		for (int i = 0; i < size - 1; i++) {
			if (i < items.size()) {
				inv.setItem(i, items.get(i));
			}else inv.setItem(i, none);
		}
	}
	
	private boolean addItem(Player p, ItemStack item, int slot) {
		items.put(slot, item);
		for (int i = 0; i < size - 1; i++) {
			ItemStack is = getInventory().getItem(i);
			if (is.equals(none)) return false;
		}
		size += 9;
		reopen(p, true);
		return true;
	}

	@Override
	public void onClick(@NotNull GuiClickEvent event) {
		if (event.hasCursor()) {
			if (event.getSlot() == size - 1)
				return; // means player wants to drop an item on the validation button

			if (none.equals(event.getClicked())) {
				getInventory().setItem(event.getSlot(), event.getCursor());
				QuestUtils.runSync(() -> {
					event.getPlayer().setItemOnCursor(null);
					addItem(event.getPlayer(), event.getCursor(), event.getSlot());
				});
			} else {
				QuestUtils.runSync(() -> items.put(event.getSlot(), event.getCursor()));
				event.setCancelled(false);
			}
		} else {
			// nothing in cursor
			if (event.getSlot() == size - 1) {
				event.close();
				end.accept(items.values().stream().filter(x -> x != null).collect(Collectors.toList()));
			}else {
				if (event.getClicked().equals(none)) {
					new ItemCreatorGUI(item -> {
						if (item != null)
							getInventory().setItem(event.getSlot(), item);
						if (!addItem(event.getPlayer(), item, event.getSlot()))
							event.reopen();
					}, true).open(event.getPlayer());
				} else {
					if (event.getClick().isLeftClick() || (event.getClick().isRightClick() && event.getClicked().getAmount() == 1)) {
						QuestUtils.runSync(() -> {
							getInventory().setItem(event.getSlot(), none);
							items.remove(event.getSlot());
						});
					} else
						QuestUtils.runSync(() -> items.put(event.getSlot(), getInventory().getItem(event.getSlot())));
					event.setCancelled(false);
				}
			}
		}
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REOPEN;
	}

}