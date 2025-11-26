package fr.skytasul.quests.gui.pools;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;

public class PoolsManageGUI extends PagedGUI<QuestPoolController> {

	private static final ItemStack CREATE_POOL = ItemUtils.item(XMaterial.SLIME_BALL, Lang.poolCreate.toString());

	private final QuestPoolsManager poolsManager;

	public PoolsManageGUI(QuestPoolsManager poolsManager) {
		super(Lang.INVENTORY_POOLS_MANAGE.toString(), DyeColor.CYAN, Collections.emptyList());
		this.poolsManager = poolsManager;
		refreshContents();
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		refreshContents();
		super.populate(player, inventory);
	}

	private void refreshContents() {
		objects = new ArrayList<>(poolsManager.getPools());
		objects.add(null); // for the creation item
	}

	@Override
	public ItemStack getItemStack(QuestPoolController object) {
		return object == null ? CREATE_POOL
				: ItemUtils.loreAdd(object.getItemStack(),
						"",
						"§8" + Lang.ClickLeft + " > §e§l" + Lang.edit,
						"§8" + Lang.ClickShiftLeft + " > §c" + Lang.Remove);
	}

	@Override
	public void click(QuestPoolController existing, ItemStack clicked, ClickType click) {
		if (click == ClickType.SHIFT_LEFT) {
			if (existing != null) {
				BeautyQuests.getInstance().getGuiManager().getFactory().createConfirmation(() -> {
					poolsManager.removePool(existing.getId());
					reopen(getViewer(), true);
				}, this::reopen, Lang.INDICATION_REMOVE_POOL.format(existing)).open(player);
			}
		}else {
			var gui = new PoolEditGUI(this::reopen, newPoolData -> {
				if (existing == null)
					poolsManager.registerPool(newPoolData);
				else
					poolsManager.editPool(existing.getId(), newPoolData);
				reopen(getViewer(), true);
			});
			if (existing != null)
				gui.fillFrom(existing.getPoolData());
			gui.open(getViewer());
		}
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}

}
