package fr.skytasul.quests.integrations.mobs;

import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.XMaterial;
import me.lokka30.levelledmobs.LevelledMobs;

public class BQLevelledMobs implements LeveledMobFactory<EntityType> {

	@Override
	public String getID() {
		return "levelledMobs";
	}

	private ItemStack item = ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, "§eLevelledMobs");

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
		return JavaPlugin.getPlugin(LevelledMobs.class).levelInterface.getLevelOfMob((LivingEntity) entity);
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
