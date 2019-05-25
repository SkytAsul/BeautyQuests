package fr.skytasul.quests.gui.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.EpicBossParser;
import fr.skytasul.quests.editors.checkers.MythicNameParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.EntityTypeGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import fr.skytasul.quests.utils.types.Mob;
import fr.skytasul.quests.utils.types.RunnableObj;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.citizensnpcs.api.npc.NPC;

public class CreateMobGUI implements CustomInventory{
	
	private ItemStack bukkitMob = ItemUtils.item(XMaterial.WITHER_SKELETON_SPAWN_EGG, Lang.bukkitMob.toString());
	private ItemStack mythicMob = ItemUtils.item(XMaterial.BLAZE_POWDER, Lang.mythicMob.toString());
	private ItemStack epicBoss = ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.epicBoss.toString());
	private ItemStack ndone = ItemUtils.item(XMaterial.CHARCOAL, Lang.done.toString());
	
	public RunnableObj run;
	
	public Inventory inv;
	private boolean done = false;
	
	private Mob mob = new Mob();
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_SELECT.toString());

		inv.setItem(0, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(1)));
		inv.setItem(1, bukkitMob);
		inv.setItem(3, SelectGUI.selectNPC.clone());
		if(Dependencies.mm) inv.setItem(5, mythicMob.clone());
		if(Dependencies.eboss) inv.setItem(6, epicBoss.clone());
		inv.setItem(8, ndone.clone());
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		case 0:
			Utils.sendMessage(p, Lang.MOB_AMOUNT.toString());
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
					int i = (int) obj;
					if (i == 0){
						Utils.sendMessage(p, Lang.NUMBER_ZERO.toString());
					}else {
						mob.amount = i;
						inv.setItem(slot, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(i)));
					}
					openLastInv(p);
			}, new NumberParser(Integer.class, true)));
			break;

		case 1:
			Inventories.create(p, new EntityTypeGUI((obj) -> {
					Inventories.put(p, openLastInv(p), inv);
					reset();
					EntityType en = (EntityType) obj;
					mob = new Mob(en, mob.amount);
					
					inv.setItem(slot, ItemUtils.item(XMaterial.mobItem(en), Lang.bukkitMob.toString(), en.getName()));
					
					if (!done) inv.setItem(8, ItemUtils.itemDone());
					done = true;
			}));
			break;
			
		case 3:
			Lang.SELECT_KILL_NPC.send(p);
			new SelectNPC(p, (obj) -> {
				openLastInv(p);
				reset();
				NPC npc = (NPC) obj;
				mob = new Mob(npc, mob.amount);
				ItemUtils.lore(current, npc.getName());
				
				if (!done) inv.setItem(8, ItemUtils.itemDone());
				done = true;
			}).enterOrLeave(p);

		case 5:
			if (!Dependencies.mm) break;
			MythicMobs.sendMythicMobsList(p);
			new TextEditor(p, (obj) -> {
				openLastInv(p);
				reset();
				MythicMob mmob = (MythicMob) obj;
				mob = new Mob(mmob, mob.amount);
				ItemUtils.lore(current, "§a" + mmob.getDisplayName());

				if (!done) inv.setItem(8, ItemUtils.itemDone());
				done = true;
			}, new MythicNameParser(), () -> {
				openLastInv(p);
			}, null).enterOrLeave(p);
			break;
			
		case 6:
			if (!Dependencies.eboss) break;
			new TextEditor(p, (obj) -> {
				openLastInv(p);
				reset();
				mob = new Mob((String) obj, mob.amount);
				ItemUtils.lore(current, "§a" + obj);

				if (!done) inv.setItem(8, ItemUtils.itemDone());
				done = true;
			}, new EpicBossParser(), () -> {
				openLastInv(p);
			}, null).enterOrLeave(p);
			break;
			
		case 8:
			if (current.getType() == Material.DIAMOND) {
				Inventories.closeAndExit(p);
				run.run(mob);
			}
			break;
			
		}
		return true;
	}
	
	private void reset(){
		if (mob.hasBukkitMob()){
			inv.setItem(1, bukkitMob);
		}else if (mob.hasEpicBoss()){
			ItemUtils.lore(inv.getItem(6));
		}else if (mob.hasMythicMob()){
			ItemUtils.lore(inv.getItem(5));
		}else if (mob.hasNPC()){
			ItemUtils.lore(inv.getItem(3));
		}
	}

}