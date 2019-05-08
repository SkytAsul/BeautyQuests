package fr.skytasul.quests.gui.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

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

	public ItemStack getItemStack(EntityType object){
		return entities.get(object);
	}

	public void click(EntityType existing){
		Inventories.closeAndExit(p);
		run.run(existing);
	}

}