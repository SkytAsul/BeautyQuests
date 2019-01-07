package fr.skytasul.quests.gui.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class EntityTypeGUI implements CustomInventory{

	private static Map<Integer, ItemStack> entities = new HashMap<>();
	private static int size;
	static {
		int i = 0;
		for (EntityType en : EntityType.values()){
			if (!en.isAlive()) continue;
			if (en == EntityType.PLAYER) continue;
			XMaterial mat = /*XMaterial.requestXMaterial("MONSTER_EGG", new SpawnEgg(en).getData());*/XMaterial.mobItem(en);
			if (mat == null) continue;
			/*inv.setItem*/entities.put(i, ItemUtils.item(mat, en.getName()));
			i++;
		}
		/*inv.setItem*/entities.put(++i, ItemUtils.skull("player", "Knight"));
		size = (int) Math.ceil(i * 1.0 / 9.0) * 9;
	}
	
	public EntityTypeGUI(RunnableObj run){
		this.run = run;
	}
	
	private RunnableObj run;
	
	private Inventory inv;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, size, Lang.INVENTORY_TYPE.toString());
		
		for (Entry<Integer, ItemStack> item : entities.entrySet()) {
			inv.setItem(item.getKey(), item.getValue());
		}
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		String name = ItemUtils.getName(current);
		Inventories.remove(p);
		run.run(name.equals("player") ? EntityType.PLAYER : EntityType.fromName(name));
	}

	public boolean onClose(Player p, Inventory inv){
		new BukkitRunnable() {
			public void run(){
				p.openInventory(inv);
			}
		}.runTaskLater(BeautyQuests.getInstance(), 1L);
		return false;
	}

}