package fr.skytasul.quests.gui.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class MobsListGUI implements CustomInventory{

	private ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.mobsNone.toString());
	
	public Map<Integer, Mob<?>> mobs = new HashMap<>();
	
	public Inventory inv;
	public Consumer<List<Mob<?>>> run;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_MOBS.toString());
		
		inv.setItem(8, ItemUtils.itemDone);
		for (int i = 0; i < 8; i++) inv.setItem(i, none.clone());
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void setMobsFromList(List<Mob<?>> m) {
		for (int i = 0; i < 8; i++){
			if (m.size() == i) break;
			mobs.put(i, m.get(i));
			inv.setItem(i, m.get(i).createItemStack());
		}
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			Inventories.closeAndExit(p);
			run.accept(new ArrayList<>(mobs.values()));
			return true;
		}
		if (click.isRightClick()){
			mobs.remove(slot);
			inv.setItem(slot, none.clone());
			return true;
		}
		new PagedGUI<MobFactory<?>>("Mob type", DyeColor.LIME, MobFactory.factories) {
			public ItemStack getItemStack(MobFactory<?> object) {
				return object.getFactoryItem();
			}

			@SuppressWarnings ("rawtypes")
			public void click(MobFactory<?> existing) {
				existing.itemClick(p, (obj) -> {
					Lang.MOB_AMOUNT.send(p);
					new TextEditor(p, (x) -> {
						Inventories.put(p, openLastInv(p), MobsListGUI.this.inv);
						Mob<?> mob = new Mob(existing, obj, (int) x);
						MobsListGUI.this.inv.setItem(slot, mob.createItemStack());
						mobs.put(slot, mob);
					}, new NumberParser(Integer.class, true, true)).enterOrLeave(p);
				});
			}
		}.create(p);
		return true;
	}

}