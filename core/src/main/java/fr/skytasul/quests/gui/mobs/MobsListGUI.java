package fr.skytasul.quests.gui.mobs;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
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

	@Override
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
			inv.setItem(id, createItemStack(mobEntry.getKey(), mobEntry.getValue()));
		}
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			Inventories.closeAndExit(p);
			run.accept(mobs);
			return true;
		}
		Entry<Mob<?>, Integer> mobEntry = mobs.get(slot);
		if (mobEntry == null) {
			new MobSelectionGUI(mob -> {
				if (mob != null) {
					inv.setItem(slot, createItemStack(mob, 1));
					mobs.put(slot, new AbstractMap.SimpleEntry<>(mob, 1));
				}
				Inventories.put(p, openLastInv(p), MobsListGUI.this.inv);
			}).create(p);
		}else {
			if (click == ClickType.SHIFT_LEFT) {
				Lang.MOB_NAME.send(p);
				new TextEditor<>(p, () -> openLastInv(p), name -> {
					mobEntry.getKey().setCustomName((String) name);
					inv.setItem(slot, createItemStack(mobEntry.getKey(), mobEntry.getValue()));
					openLastInv(p);
				}).passNullIntoEndConsumer().enter();
			}else if (click == ClickType.LEFT) {
				Lang.MOB_AMOUNT.send(p);
				new TextEditor<>(p, () -> openLastInv(p), amount -> {
					mobEntry.setValue(amount);
					inv.setItem(slot, createItemStack(mobEntry.getKey(), amount));
					openLastInv(p);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			} else if (click == ClickType.SHIFT_RIGHT) {
				if (mobEntry.getKey().getFactory() instanceof LeveledMobFactory) {
					new TextEditor<>(p, () -> openLastInv(p), level -> {
						mobEntry.getKey().setMinLevel(level);
						inv.setItem(slot, createItemStack(mobEntry.getKey(), mobEntry.getValue()));
						openLastInv(p);
					}, new NumberParser<>(Double.class, true, false)).enter();
				} else {
					Utils.playPluginSound(p.getLocation(), "ENTITY_VILLAGER_NO", 0.6f);
				}
			} else if (click == ClickType.RIGHT) {
				mobs.remove(slot);
				inv.setItem(slot, none.clone());
			}
		}
		return true;
	}
	
	private ItemStack createItemStack(Mob mob, int amount) {
		List<String> lore = new ArrayList<>();
		lore.add(Lang.Amount.format(amount));
		lore.addAll(mob.getFactory().getDescriptiveLore(mob.getData()));
		lore.add("");
		lore.add(Lang.click.toString());
		if (mob.getFactory() instanceof LeveledMobFactory) {
			lore.add("§7" + Lang.ClickShiftRight + " > §e" + Lang.setLevel);
		} else {
			lore.add("§8§n" + Lang.ClickShiftRight + " > " + Lang.setLevel);
		}
		ItemStack item = ItemUtils.item(mob.getMobItem(), mob.getName(), lore);
		item.setAmount(Math.min(amount, 64));
		return item;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
	}
	
}