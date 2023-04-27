package fr.skytasul.quests.gui.mobs;

import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.mobs.Mob;

public class MobSelectionGUI extends PagedGUI<MobFactory<?>> {
	
	private Consumer<Mob<?>> end;
	
	public MobSelectionGUI(Consumer<Mob<?>> end) {
		super(Lang.INVENTORY_MOBSELECT.toString(), DyeColor.LIME, MobFactory.factories);
		this.end = end;
	}
	
	@Override
	public ItemStack getItemStack(MobFactory<?> object) {
		return object.getFactoryItem();
	}
	
	@Override
	public void click(MobFactory<?> existing, ItemStack item, ClickType clickType) {
		existing.itemClick(player, mobData -> {
			end.accept(mobData == null ? null : new Mob(existing, mobData));
		});
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(() -> end.accept(null));
	}
	
}
