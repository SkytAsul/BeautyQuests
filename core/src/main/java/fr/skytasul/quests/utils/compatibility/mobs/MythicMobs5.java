package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;

public class MythicMobs5 implements LeveledMobFactory<MythicMob>, Listener {

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
		new PagedGUI<MythicMob>("List of MythicMobs", DyeColor.PINK, MythicBukkit.inst().getMobManager().getMobTypes(), null, MythicMob::getInternalName) {
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
		return MythicBukkit.inst().getMobManager().getMythicMob(value).orElse(null);
	}
	
	@Override
	public boolean bukkitMobApplies(MythicMob first, Entity entity) {
		return MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId())
				.map(mob -> mob.getType().equals(first))
				.orElse(false);
	}

	@Override
	public double getMobLevel(MythicMob type, Entity entity) {
		return MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).get().getLevel();
	}

	@Override
	public String getValue(MythicMob data) {
		return data.getInternalName();
	}

	@Override
	public String getName(MythicMob data) {
		PlaceholderString displayName = data.getDisplayName();
		if (displayName != null) return displayName.get();
		return data.getInternalName();
	}

	@Override
	public EntityType getEntityType(MythicMob data) {
		String typeName;
		if (data.getEntityType() == null) {
			typeName = data.getMythicEntity() == null ? null : data.getMythicEntity().getClass().getSimpleName().substring(6);
		}else {
			typeName = Objects.toString(data.getEntityType()).toUpperCase();
		}
		if (typeName == null)
			return null;

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
					QuestOption.formatDescription("Base Health: " + Objects.toString(data.getHealth())),
					QuestOption.formatDescription("Base Damage: " + Objects.toString(data.getDamage())),
					QuestOption.formatDescription("Base Armor: " + Objects.toString(data.getArmor())));
		} catch (Throwable ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error occurred while showing mob description", ex);
			return Arrays.asList("§cError when retrieving mob informations");
		}
	}

	@EventHandler
	public void onMythicDeath(MythicMobDeathEvent e) {
		if (e.getKiller() == null) return;
		if (!(e.getKiller() instanceof Player)) return;
		callEvent(e, e.getMob().getType(), e.getEntity(), (Player) e.getKiller());
	}
	
	public static void sendMythicMobsList(Player p){
		Lang.MYTHICMOB_LIST.send(p);
		StringBuilder stb = new StringBuilder("§a");
		for (MythicMob mm : MythicBukkit.inst().getMobManager().getMobTypes()) {
			stb.append(mm.getInternalName() + "; ");
		}
		MessageUtils.sendUnprefixedMessage(p, stb.toString());
	}
	
}
