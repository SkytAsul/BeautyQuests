package fr.skytasul.quests.gui.mobs;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
	
	public Map<Integer, Entry<Mob<?>, Integer>> mobs = new HashMap<>();
	
	public Inventory inv;
	public Consumer<Map<Integer, Entry<Mob<?>, Integer>>> run;
	
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
	
	public void setMobsFromMap(Map<Integer, Entry<Mob<?>, Integer>> map) {
		for (Entry<Integer, Entry<Mob<?>, Integer>> entry : map.entrySet()) {
			int id = entry.getKey();
			Entry<Mob<?>, Integer> mobEntry = entry.getValue();
			mobs.put(id, mobEntry);
			inv.setItem(id, mobEntry.getKey().createItemStack(mobEntry.getValue()));
		}
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			Inventories.closeAndExit(p);
			run.accept(mobs);
			return true;
		}
		if (click.isRightClick()){
			mobs.remove(slot);
			inv.setItem(slot, none.clone());
			return true;
		}
		new PagedGUI<MobFactory<?>>(Lang.INVENTORY_MOBSELECT.toString(), DyeColor.LIME, MobFactory.factories) {
			public ItemStack getItemStack(MobFactory<?> object) {
				return object.getFactoryItem();
			}

			@SuppressWarnings ("rawtypes")
			public void click(MobFactory<?> existing, ItemStack item, ClickType clickType) {
				existing.itemClick(p, (obj) -> {
					if (obj == null) {
						Inventories.put(p, openLastInv(p), MobsListGUI.this.inv);
						return;
					}
					Lang.MOB_AMOUNT.send(p);
					new TextEditor<>(p, () -> Inventories.put(p, openLastInv(p), MobsListGUI.this.inv), amount -> {
						Inventories.put(p, openLastInv(p), MobsListGUI.this.inv);
						Mob<?> mob = new Mob(existing, obj);
						MobsListGUI.this.inv.setItem(slot, mob.createItemStack(amount));
						mobs.put(slot, new AbstractMap.SimpleEntry<>(mob, amount));
					}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
				});
			}
		}.create(p);
		return true;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
	}
	
}