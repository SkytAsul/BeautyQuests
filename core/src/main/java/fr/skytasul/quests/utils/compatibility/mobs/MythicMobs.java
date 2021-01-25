package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.nms.NMS;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;

public class MythicMobs implements MobFactory<MythicMob> {

	public String getID() {
		return "mythicMobs";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_POWDER, Lang.mythicMob.toString());
	public ItemStack getFactoryItem() {
		return item;
	}

	public void itemClick(Player p, Consumer<MythicMob> run) {
		new PagedGUI<MythicMob>("List of MythicMobs", DyeColor.PINK, io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMobTypes(), null, x -> x.getInternalName()) {
			public ItemStack getItemStack(MythicMob object) {
				XMaterial mobItem;
				try {
					mobItem = XMaterial.mobItem(getEntityType(object));
				}catch (Exception ex) {
					mobItem = XMaterial.SPONGE;
					BeautyQuests.logger.warning("Unknow entity type for MythicMob " + object.getInternalName());
					ex.printStackTrace();
				}
				return ItemUtils.item(mobItem, object.getInternalName());
			}

			public void click(MythicMob existing, ItemStack item, ClickType clickType) {
				Inventories.closeAndExit(p);
				run.accept(existing);
			}
		}.create(p);
	}

	public MythicMob fromValue(String value) {
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMob(value);
	}

	public String getValue(MythicMob data) {
		return data.getInternalName();
	}

	public String getName(MythicMob data) {
		try {
			PlaceholderString displayName = data.getDisplayName();
			if (displayName != null) return displayName.get();
		}catch (NoSuchMethodError e) {}
		return data.getInternalName();
	}

	public EntityType getEntityType(MythicMob data) {
		String typeName = data.getEntityType().toUpperCase();
		if (typeName.contains("BABY_")) typeName = typeName.substring(5);
		if (typeName.equalsIgnoreCase("MPET")) typeName = data.getConfig().getString("MPet.Anchor");
		if (NMS.getMCVersion() < 11 && typeName.equals("WITHER_SKELETON")) typeName = "SKELETON";
		EntityType type = EntityType.fromName(typeName);
		if (type == null) type = EntityType.valueOf(typeName);
		return type;
	}

	public List<String> getDescriptiveLore(MythicMob data) {
		try {
			return Arrays.asList("Base Health: " + data.getHealth().get(), "Base Damage: " + data.getDamage().get(), "Base Armor: " + data.getArmor().get());
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
	
	public static void sendMythicMobsList(Player p){
		Utils.sendMessage(p, Lang.MYTHICMOB_LIST.toString());
		StringBuilder stb = new StringBuilder("§a");
		for (MythicMob mm : io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMobTypes()) {
			stb.append(mm.getInternalName() + "; ");
		}
		Utils.sendMessage(p, stb.toString());
	}
	
}
