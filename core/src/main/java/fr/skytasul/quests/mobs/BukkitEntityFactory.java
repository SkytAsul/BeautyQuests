package fr.skytasul.quests.mobs;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.utils.MinecraftNames;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BukkitEntityFactory implements MobFactory<EntityType>, Listener {

	@Override
	public String getID() {
		return "bukkitEntity";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_SPAWN_EGG, Lang.bukkitMob.toString());
	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<EntityType> run) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createEntityTypeSelection(run, x -> x != null).open(p);
	}

	@Override
	public boolean bukkitMobApplies(EntityType first, Entity entity) {
		return entity.getType() == first;
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
	public List<String> getDescriptiveLore(EntityType data) {
		return Arrays.asList(Lang.EntityType.quickFormat("entity_type", MinecraftNames.getEntityName(data)));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityKilled(EntityDeathEvent e) {
		LivingEntity en = e.getEntity();
		if (en.getKiller() == null) return;
		callEvent(e, en.getType(), en, en.getKiller());
	}

}
