package fr.skytasul.quests.integrations.mobs;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MythicMobs implements LeveledMobFactory<MythicMob>, Listener {

	@Override
	public String getID() {
		return "mythicMobs";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_POWDER, Lang.mythicMob.toString());
	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<MythicMob> run) {
		new PagedGUI<MythicMob>("List of MythicMobs", DyeColor.PINK, io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMobTypes(), null, MythicMob::getInternalName) {
			@Override
			public ItemStack getItemStack(MythicMob object) {
				XMaterial mobItem;
				try {
					mobItem = Utils.mobItem(getEntityType(object));
				}catch (Exception ex) {
					mobItem = XMaterial.SPONGE;
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Unknow entity type for MythicMob " + object.getInternalName(), ex);
				}
				return ItemUtils.item(mobItem, object.getInternalName());
			}

			@Override
			public void click(MythicMob existing, ItemStack item, ClickType clickType) {
				close();
				run.accept(existing);
			}
		}.sortValues(MythicMob::getInternalName).open(p);
	}

	@Override
	public MythicMob fromValue(String value) {
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMob(value);
	}
	
	@Override
	public boolean bukkitMobApplies(MythicMob first, Entity entity) {
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId())
				.map(mob -> mob.getType().equals(first))
				.orElse(false);
	}

	@Override
	public double getMobLevel(MythicMob type, Entity entity) {
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).get()
				.getLevel();
	}

	@Override
	public String getValue(MythicMob data) {
		return data.getInternalName();
	}

	@Override
	public String getName(MythicMob data) {
		try {
			PlaceholderString displayName = data.getDisplayName();
			if (displayName != null) return displayName.get();
		}catch (NoSuchMethodError e) {}
		return data.getInternalName();
	}

	@Override
	public EntityType getEntityType(MythicMob data) {
		String typeName;
		if (data.getEntityType() == null) {
			typeName = data.getMythicEntity() == null ? null : data.getMythicEntity().getClass().getSimpleName().substring(6);
		}else {
			typeName = data.getEntityType().toUpperCase();
		}
		if (typeName.contains("BABY_")) typeName = typeName.substring(5);
		if (typeName.equalsIgnoreCase("MPET")) typeName = data.getConfig().getString("MPet.Anchor");
		if (MinecraftVersion.MAJOR < 11 && typeName.equals("WITHER_SKELETON")) typeName = "SKELETON";
		EntityType type = EntityType.fromName(typeName);
		if (type == null) {
			try {
				type = EntityType.valueOf(typeName);
			}catch (IllegalArgumentException ex) {}
		}
		return type;
	}

	@Override
	public List<String> getDescriptiveLore(MythicMob data) {
		try {
			return Arrays.asList(
					QuestOption.formatDescription("Base Health: " + data.getHealth().get()),
					QuestOption.formatDescription("Base Damage: " + data.getDamage().get()),
					QuestOption.formatDescription("Base Armor: " + data.getArmor().get()));
		}catch (NoSuchMethodError e) {
			return Arrays.asList("§cError when retrieving mob informations", "§c-> §oPlease update MythicMobs");
		}
	}

	@EventHandler
	public void onMythicDeath(MythicMobDeathEvent e) {
		if (e.getKiller() == null) return;
		if (!(e.getKiller() instanceof Player)) return;
		callEvent(e, e.getMob().getType(), e.getEntity(), (Player) e.getKiller());
	}
	
}
