package fr.skytasul.quests.gui.pools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class PoolsManageGUI extends PagedGUI<QuestPool> {
	
	private static final ItemStack CREATE_POOL = ItemUtils.item(XMaterial.SLIME_BALL, Lang.poolCreate.toString());
	
	private PoolsManageGUI(Collection<QuestPool> objects) {
		super(Lang.INVENTORY_POOLS_MANAGE.toString(), DyeColor.CYAN, objects);
	}
	
	@Override
	public ItemStack getItemStack(QuestPool object) {
		return object == null ? CREATE_POOL : object.getItemStack(Lang.poolEdit.toString());
	}
	
	@Override
	public void click(QuestPool existing, ItemStack clicked, ClickType click) {
		if (click == ClickType.MIDDLE) {
			if (existing != null) {
				BeautyQuests.getInstance().getPoolsManager().removePool(existing.getID());
				get().create(p);
			}
		}else {
			new PoolEditGUI(() -> get().create(p), existing).create(p);
		}
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REMOVE;
	}
	
	public static PoolsManageGUI get() {
		List<QuestPool> pools = new ArrayList<>(BeautyQuests.getInstance().getPoolsManager().getPools());
		pools.add(null);
		return new PoolsManageGUI(pools);
	}
	
}
