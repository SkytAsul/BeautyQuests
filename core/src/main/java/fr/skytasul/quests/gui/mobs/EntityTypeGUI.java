package fr.skytasul.quests.gui.mobs;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.StaticPagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityTypeGUI extends StaticPagedGUI<EntityType> {

	private static Map<EntityType, ItemStack> entities = new HashMap<>();
	static {
		for (EntityType en : EntityType.values()){
			if (!en.isAlive()) continue;
			if (en == EntityType.PLAYER) continue;
			XMaterial mat = Utils.mobItem(en);
			if (mat == null) continue;
			entities.put(en, ItemUtils.item(mat, en.getName()));
		}
		entities.put(EntityType.PLAYER, ItemUtils.skull("player", "Knight"));
		entities.put(null, ItemUtils.item(XMaterial.ENDER_EYE, Lang.EntityTypeAny.toString()));
	}

	private Consumer<EntityType> run;

	public EntityTypeGUI(Consumer<EntityType> run, Predicate<EntityType> typeFilter) {
		super(Lang.INVENTORY_TYPE.toString(), DyeColor.PURPLE, entities
				.keySet()
				.stream()
				.filter(typeFilter == null ? __ -> true : typeFilter)
				.collect(Collectors.toMap(x -> x, entities::get)), run);
		addSearchButton(EntityTypeGUI::getName, true);
		this.run = run;
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REOPEN;
	}

	private static String getName(EntityType object) {
		return object == null ? "any" : object.getName();
	}

}