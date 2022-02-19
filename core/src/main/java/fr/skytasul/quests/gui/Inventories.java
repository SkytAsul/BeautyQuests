package fr.skytasul.quests.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.utils.Lang;
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
	 * @param <T> Class who implements the CustomInventory interface
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
		
		Pair<CustomInventory, Inventory> pair = g.get(p);
		if (pair == null) return;
		if (inv == null) return;
		
		e.setCancelled(false);
		
		try {
			if (inv == p.getInventory()) {
				if (e.isShiftClick()) e.setCancelled(true);
				return;
			}
			
			ClickType click = e.getClick();
			if (click == ClickType.NUMBER_KEY || click == ClickType.DOUBLE_CLICK || click == ClickType.DROP || click == ClickType.CONTROL_DROP || click.name().equals("SWAP_OFFHAND")) { // SWAP_OFFHAND introduced in 1.16
				e.setCancelled(true);
				return;
			}
			
			if (!inv.equals(pair.getValue())) return;
			
			if (e.getCursor().getType() == Material.AIR) {
				if (current == null || current.getType() == Material.AIR) return;
				if (pair.getKey().onClick(p, inv, current, e.getSlot(), click)) e.setCancelled(true);
			}else {
				if (pair.getKey().onClickCursor(p, inv, current, e.getCursor(), e.getSlot())) e.setCancelled(true);
			}
		}catch (Exception ex) {
			e.setCancelled(true);
			ex.printStackTrace();
			Lang.ERROR_OCCURED.send(p, ex.getMessage() + " in " + pair.getKey().getClass().getSimpleName());
			BeautyQuests.logger.severe("An error occurred when " + p.getName() + " clicked in inventory " + pair.getKey().getClass().getSimpleName());
		}
	}
	
	public static void onDrag(InventoryDragEvent e) {
		if (g.get(e.getWhoClicked()) == null) return;
		e.setCancelled(true);
	}

	public static void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (close){
			close = false;
			return;
		}
		if (g.containsKey(p)) {
			Pair<CustomInventory, Inventory> pair = g.get(p);
			if (!e.getInventory().equals(pair.getValue())) return;
			switch (pair.getKey().onClose(p, e.getInventory())) {
			case REMOVE:
				remove(p);
				break;
			case REOPEN:
				new BukkitRunnable() {
					@Override
					public void run(){
						p.openInventory(e.getInventory());
					}
				}.runTaskLater(BeautyQuests.getInstance(), 1L);
				break;
			case CONFIRM:
				new BukkitRunnable() {
					@Override
					public void run(){
						create(p, new ConfirmGUI(() -> {
							remove(p);
							p.closeInventory();
						}, () -> {
							g.put(p, pair);
							p.openInventory(e.getInventory());
						}, Lang.INDICATION_CLOSE.toString()));
					}
				}.runTaskLater(BeautyQuests.getInstance(), 1L);
				break;
			case NOTHING:
				break;
			}
		}
	}
	
	public static void onOpen(InventoryOpenEvent e) {
		if (!e.isCancelled()) return;
		HumanEntity p = e.getPlayer();
		if (g.containsKey(p)) {
			BeautyQuests.logger.warning("The opening of a BeautyQuests menu for player " + p.getName() + " has been cancelled by another plugin.");
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
	
	public static void closeAndExit(Player p){
		remove(p);
		p.closeInventory();
	}
	
	
	private static void remove(Player p){
		g.remove(p);
	}
	
	public static void closeAll(){
		for (Iterator<Player> iterator = g.keySet().iterator(); iterator.hasNext();) {
			Player p = iterator.next();
			iterator.remove();
			p.closeInventory();
		}
	}
	
	public static void put(Player p, CustomInventory cinv, Inventory inv){
		g.put(p, new Pair<CustomInventory, Inventory>(cinv, inv));
	}
	
	public static boolean isInSystem(Player p){
		return g.containsKey(p);
	}
	
	public static void openInventory(Player p){
		p.openInventory(g.get(p).getValue());
	}

}