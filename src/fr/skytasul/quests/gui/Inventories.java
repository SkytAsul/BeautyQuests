package fr.skytasul.quests.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.utils.types.Pair;

public class Inventories{

	private static Map<Player, Pair<CustomInventory, Inventory>> g = new HashMap<>();

	private static boolean close = false;

	public static Inventory createGetInv(Player p, CustomInventory inv){
		put(p, inv, inv.open(p));
		return g.get(p).getValue();
	}
	
	/**
	 * Open a CustomInventory to player, and insert it to the Inventories system.
	 * @param p Player to open
	 * @param inv CustomInventory instance to open
	 * @return Same CustomInventory
	 */
	public static <T extends CustomInventory> T create(Player p, T inv) {
		closeWithoutExit(p);
		Inventory tinv = inv.open(p);
		if (tinv == null) return inv;
		put(p, inv, tinv);
		return inv;
	}
	
	public static void onClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		Inventory inv = e.getClickedInventory();
		ItemStack current = e.getCurrentItem();

		if (g.get(p) == null) return;
		if (inv == null) return;
		
		if (inv == p.getInventory()){
			if (e.isShiftClick()) e.setCancelled(true);
			return;
		}

		if (!inv.equals(g.get(p).getValue())) return;

		if (e.getCursor().getType() == Material.AIR) {
			if (current == null || current.getType() == Material.AIR) return;
			g.get(p).getKey().onClick(p, inv, current, e.getSlot(), e.getClick());
			if (g.containsKey(p))
				if (g.get(p).getKey().cancelClick())
					e.setCancelled(true);
		}else {
			if (g.get(p).getKey().onClickCursor(p, inv, current, e.getCursor(), e.getSlot()))
				e.setCancelled(true);
		}
	}

	public static void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (close){
			close = false;
			return;
		}
		if (g.containsKey(p)) {
			if (!e.getInventory().equals(g.get(p).getValue())) return;
			if (g.get(p).getKey().onClose(p, e.getInventory())) {
				remove(p);
			}
		}
	}
	
	
	public static void closeWithoutExit(Player p){
		if (!g.containsKey(p)) return;
		if (p.getOpenInventory().getType() == InventoryType.CRAFTING){
			return;
		}
		close = true;
		p.closeInventory();
	}
	
	
	public static void remove(Player p){
		g.remove(p);
	}
	
	public static void put(Player p, CustomInventory cinv, Inventory inv){
		g.put(p, new Pair<CustomInventory, Inventory>(cinv, inv));
	}
	
	public static boolean isInSystem(Player p){
		return g.containsKey(p);
	}

}