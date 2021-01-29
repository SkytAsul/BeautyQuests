package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;

public class BukkitEntityFactory implements MobFactory<EntityType> {

	public String getID() {
		return "bukkitEntity";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_SPAWN_EGG, Lang.bukkitMob.toString());
	public ItemStack getFactoryItem() {
		return item;
	}

	public void itemClick(Player p, Consumer<EntityType> run) {
		new EntityTypeGUI(run, x -> x != null).create(p);
	}

	public EntityType fromValue(String value) {
		return EntityType.valueOf(value);
	}

	public String getValue(EntityType data) {
		return data.name();
	}

	public String getName(EntityType data) {
		return MinecraftNames.getEntityName(data);
	}

	public EntityType getEntityType(EntityType data) {
		return data;
	}
	
	public List<String> getDescriptiveLore(EntityType data) {
		return Arrays.asList(Lang.EntityType.format(MinecraftNames.getEntityName(data)));
	}

	@EventHandler
	public void onEntityKilled(EntityDeathEvent e) {
		LivingEntity en = e.getEntity();
		if (en.getKiller() == null) return;
		callEvent(e, en.getType(), en, en.getKiller());
	}
	
}
