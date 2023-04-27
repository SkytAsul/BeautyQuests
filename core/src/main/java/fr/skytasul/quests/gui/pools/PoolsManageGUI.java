package fr.skytasul.quests.gui.pools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.pools.QuestPool;

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
				BeautyQuests.getInstance().getPoolsManager().removePool(existing.getId());
				get().open(player);
			}
		}else {
			new PoolEditGUI(() -> get().open(player), existing).open(player);
		}
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}
	
	public static PoolsManageGUI get() {
		List<QuestPool> pools = new ArrayList<>(BeautyQuests.getInstance().getPoolsManager().getPools());
		pools.add(null);
		return new PoolsManageGUI(pools);
	}
	
}
