package fr.skytasul.quests.gui.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class EntityTypeGUI extends PagedGUI<EntityType>{

	private static Map<EntityType, ItemStack> entities = new HashMap<>();
	static {
		for (EntityType en : EntityType.values()){
			if (!en.isAlive()) continue;
			if (en == EntityType.PLAYER) continue;
			XMaterial mat = XMaterial.mobItem(en);
			if (mat == null) continue;
			entities.put(en, ItemUtils.item(mat, en.getName()));
		}
		entities.put(EntityType.PLAYER, ItemUtils.skull("player", "Knight"));
	}
	
	private RunnableObj run;
	
	public EntityTypeGUI(RunnableObj run){
		super(Lang.INVENTORY_TYPE.toString(), DyeColor.PURPLE, new ArrayList<>(entities.keySet()));
		this.run = run;
	}
	
	public boolean onClose(Player p, Inventory inv){
		new BukkitRunnable() {
			public void run(){
				p.openInventory(inv);
			}
		}.runTaskLater(BeautyQuests.getInstance(), 1L);
		return false;
	}

	public ItemStack getItemStack(EntityType object){
		return entities.get(object);
	}

	public void click(EntityType existing){
		Inventories.remove(p);
		run.run(existing);
	}

}