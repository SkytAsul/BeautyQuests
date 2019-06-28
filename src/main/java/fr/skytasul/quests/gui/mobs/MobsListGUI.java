package fr.skytasul.quests.gui.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.mobs.EpicBosses;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.Mob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

public class MobsListGUI implements CustomInventory{
	
	private ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.mobsNone.toString());
	
	public Map<Integer, Mob> mobs = new HashMap<>();
	
	public Inventory inv;
	public Consumer<List<Mob>> run;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_MOBS.toString());
		
		inv.setItem(8, ItemUtils.itemDone());
		for (int i = 0; i < 8; i++) inv.setItem(i, none.clone());
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void setMobsFromList(List<Mob> m){
		for (int i = 0; i < 8; i++){
			if (m.size() == i) break;
			mobs.put(i, m.get(i));
			inv.setItem(i, fromMob(m.get(i)));
		}
	}

	static ItemStack fromMob(Mob mob){
		if (mob == null) return null;
		String lore = Lang.Amount.format(mob.amount);
		if (mob.hasBukkitMob()){
			String name = mob.getBukkitMob().getName();
			if (name == null) name = "player";
			return ItemUtils.item(XMaterial.mobItem(mob.getBukkitMob()), name, lore, Lang.EntityType.format(name), "", Lang.click.toString());
		}else if (mob.hasMythicMob()){
			MythicMob mm = mob.getMythicMob();
			String typeName = mm.getEntityType().toUpperCase();
			if (typeName.contains("BABY_")) typeName = typeName.substring(5);
			if (typeName.equalsIgnoreCase("MPET")) typeName = mm.getConfig().getString("MPet.Anchor");
			if (NMS.getMCVersion() < 11 && typeName == "WITHER_SKELETON") typeName = "SKELETON";
			EntityType type = EntityType.fromName(typeName);
			if (type == null) type = EntityType.valueOf(typeName);
			return ItemUtils.item(XMaterial.mobItem(type), MythicMobs.getDisplayName(mm), lore, "Base Health : " + mm.getBaseHealth(), "Base Damage : " + mm.getBaseDamage(), "Base Armor : " + mm.getBaseArmor(), "", Lang.click.toString());
		}else if (mob.hasNPC()){
			NPC npc = mob.getNPC();
			return ItemUtils.item(XMaterial.mobItem(npc.getTrait(MobType.class).getType()), npc.getName(), lore, "Name : " + npc.getName(), "NPC ID : " + npc.getId(), "", Lang.click.toString());
		}else if (mob.hasEpicBoss()){
			return ItemUtils.item(XMaterial.mobItem(EpicBosses.getBossEntityType(mob.getBossName())), mob.getBossName(), lore, "", Lang.click.toString());
		}else return null;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			Inventories.closeAndExit(p);
			run.accept(new ArrayList<>(mobs.values()));
			return true;
		}
		if (click.isRightClick()){
			mobs.remove(slot);
			inv.setItem(slot, none.clone());
			return true;
		}
		CreateMobGUI sm = Inventories.create(p, new CreateMobGUI());
		sm.run = (obj) -> {
			Inventories.put(p, openLastInv(p), inv);
			inv.setItem(slot, fromMob(obj));
			mobs.put(slot, obj);
		};
		return true;
	}

}