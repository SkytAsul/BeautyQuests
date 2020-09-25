package fr.skytasul.quests.gui.misc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.XMaterial;

public class BranchesGUI implements CustomInventory {
	
	private Branch main = new Branch();
	
	private Inventory inv;
	private Branch shown;
	
	private int xStart = 0;
	
	@Override
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, 54, "Branches");
		
		inv.setItem(52, ItemUtils.itemLaterPage);
		inv.setItem(53, ItemUtils.itemNextPage);
		
		// TODO voir fichier image schéma
		
		displayBranch(main);
		
		return p.openInventory(inv).getTopInventory();
	}
	
	private void displayBranch(Branch branch) {
		this.shown = branch;
		for (int i = 0; i < 45; i++) {
			inv.clear(i);
		}
		
		int y = 2;
		int start = y * 9;
		//int to = Math.min(xStart + 9, shown.choices.size());
		boolean create = true;
		int to;
		if (shown.choices.size() >= xStart + 9) {
			to = xStart + 9;
			create = false;
		}else {
			to = shown.choices.size();
		}
		for (int i = xStart; i < to; i++) {
			Thing thing = shown.things.get(i);
			inv.setItem(start + i, thing.getItem(i == to - 1 ? shown.choices.size() == 0 ? XMaterial.RED_STAINED_GLASS_PANE : XMaterial.ORANGE_STAINED_GLASS_PANE : XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));
		}
		if (!create) return;
		if (shown.choices.size() == 0) {
			inv.setItem(start + to, ItemUtils.item(XMaterial.SLIME_BALL, "§eCreate next thing", "§8> LEFT CLICK : §7Create normal thing", "§8> RIGHT CLICK : §7Create choices"));
		}else {
			int i = 0;
			for (Entry<Thing, Branch> entry : shown.choices.entrySet()) {
				inv.setItem(i * 9 + to, entry.getKey().getItem(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));
				i++;
			}
			for (; i < 5; i++) {
				inv.setItem(i * 9 + to, ItemUtils.item(XMaterial.SLIME_BALL, "§6Create choice"));
			}
		}
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot == 52) {
			if (xStart > 0) {
				xStart--;
				displayBranch(shown);
			}
		}else if (slot == 53) {
			xStart++;
			displayBranch(shown);
		}
		return true;
	}
	
	static class Branch {
		LinkedList<Thing> things = new LinkedList<>();
		Map<Thing, Branch> choices = new HashMap<>();
	}
	
	static class Thing {
		private static int counter = 0;
		
		private int id = counter++;
		
		ItemStack getItem(XMaterial type) {
			return ItemUtils.item(type, "§4Thing §b§l" + id);
		}
	}
	
}
