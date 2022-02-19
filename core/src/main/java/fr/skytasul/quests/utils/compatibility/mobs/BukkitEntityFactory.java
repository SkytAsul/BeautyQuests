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

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;

public class BukkitEntityFactory implements MobFactory<EntityType> {

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
		new EntityTypeGUI(run, x -> x != null).create(p);
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
		return Arrays.asList(Lang.EntityType.format(MinecraftNames.getEntityName(data)));
	}

	@EventHandler
	public void onEntityKilled(EntityDeathEvent e) {
		LivingEntity en = e.getEntity();
		if (en.getKiller() == null) return;
		if (QuestsAPI.getNPCsManager().isNPC(en)) return;
		callEvent(e, en.getType(), en, en.getKiller());
	}
	
}
