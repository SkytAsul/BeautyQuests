package fr.skytasul.quests.integrations.mobs;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class BQLevelledMobs implements LeveledMobFactory<EntityType> {

	private final NamespacedKey levelKey;

	private final ItemStack item = ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, "§eLevelledMobs");

	public BQLevelledMobs() {
		this.levelKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("LevelledMobs"), "level");
	}

	@Override
	public String getID() {
		return "levelledMobs";
	}

	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<EntityType> run) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createEntityTypeSelection(run, x -> x != null && x.isAlive())
				.open(p);
	}

	@Override
	public double getMobLevel(EntityType type, Entity entity) {
		Integer level = entity.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
		return level == null ? 0 : level.intValue();
		// return JavaPlugin.getPlugin(LevelledMobs.class).getLevelInterface().getLevelOfMob((LivingEntity)
		// entity);
	}

	@Override
	public EntityType fromValue(String value) {
		return EntityType.valueOf(value);
	}

	@Override
	public String getValue(EntityType data) {
		return data.name();
	}

	@Override
	public String getName(EntityType data) {
		return MinecraftNames.getEntityName(data);
	}

	@Override
	public EntityType getEntityType(EntityType data) {
		return data;
	}

	@Override
	public boolean bukkitMobApplies(@NotNull EntityType first, @NotNull Entity entity) {
		return entity.getType() == first;
	}

	// we do not have to bother about listening to EntityDeathEvent as the integrated
	// BukkitEntityFactory will manage the event by itself

}
