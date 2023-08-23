package fr.skytasul.quests.gui.mobs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;

public class EntityTypeGUI extends PagedGUI<EntityType>{

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
				.collect(Collectors.toList()), null, EntityTypeGUI::getName);
		sortValues(EntityType::getName);
		this.run = run;
	}

	@Override
	public ItemStack getItemStack(EntityType object){
		return entities.get(object);
	}

	@Override
	public void click(EntityType existing, ItemStack item, ClickType clickType){
		close(player);
		run.accept(existing);
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REOPEN;
	}

	private static String getName(EntityType object) {
		return object == null ? "any" : object.getName();
	}

}