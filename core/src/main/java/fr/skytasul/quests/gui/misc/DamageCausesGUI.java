package fr.skytasul.quests.gui.misc;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.DyeColor;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.gui.templates.StaticPagedGUI;
import fr.skytasul.quests.api.localization.Lang;

public class DamageCausesGUI extends ListGUI<DamageCause> {
	
	private static final Map<DamageCause, ItemStack> MAPPED_ITEMS;
	
	static {
		MAPPED_ITEMS = new EnumMap<>(DamageCause.class);
		for (DamageCause cause : DamageCause.values()) {
			XMaterial type;
			if (cause.name().length() <= 6) {
				type = XMaterial.CREEPER_HEAD;
			}else if (cause.name().length() < 10) {
				type = XMaterial.ZOMBIE_HEAD;
			}else if (cause.name().length() < 14) {
				type = XMaterial.SKELETON_SKULL;
			}else {
				type = XMaterial.WITHER_SKELETON_SKULL;
			}
			// this is simply not to have a fully uniform list
			MAPPED_ITEMS.put(cause, ItemUtils.item(type, "§7" + cause.name()));
		}
	}
	
	private final Consumer<List<DamageCause>> end;

	public DamageCausesGUI(List<DamageCause> causes, Consumer<List<DamageCause>> end) {
		super(Lang.INVENTORY_DAMAGE_CAUSE.toString(), DyeColor.RED, causes);
		this.end = end;
	}
	
	@Override
	public void finish(List<DamageCause> objects) {
		end.accept(objects);
	}
	
	@Override
	public ItemStack getObjectItemStack(DamageCause object) {
		return MAPPED_ITEMS.get(object);
	}
	
	@Override
	public void createObject(Function<DamageCause, ItemStack> callback) {
		new StaticPagedGUI<DamageCause>(Lang.INVENTORY_DAMAGE_CAUSES_LIST.toString(), DyeColor.ORANGE, MAPPED_ITEMS,
				callback::apply, DamageCause::name).open(player);
	}
	
}
