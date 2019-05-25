package fr.skytasul.quests.gui.npc;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.EntityTypeGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

public class NPCGUI implements CustomInventory{

	private ItemStack name = ItemUtils.item(XMaterial.NAME_TAG, Lang.name.toString());
	private ItemStack skin = ItemUtils.skull(Lang.skin.toString(), "Knight");
	private ItemStack type = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.type.toString(), "villager");
	private ItemStack move = ItemUtils.item(XMaterial.MINECART, Lang.move.toString(), Lang.moveLore.toString());
	public static ItemStack validMove = ItemUtils.item(XMaterial.EMERALD, Lang.moveItem.toString());
	
	public RunnableObj run;
	public Inventory inv;
	
	private EntityType en = EntityType.VILLAGER;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_NPC.toString());
		
		inv.setItem(0, move.clone());
		inv.setItem(1, name.clone());
		inv.setItem(3, skin.clone());
		inv.setItem(5, type.clone());
		inv.setItem(7, ItemUtils.itemCancel());
		inv.setItem(8, ItemUtils.itemDone());
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		switch (slot){
		
		case 0:
			Editor.enterOrLeave(p, new WaitClick(p, () -> {
				openLastInv(p);
			}, validMove.clone()));
			break;

		case 1:
			Lang.NPC_NAME.send(p);
			new TextEditor(p, (obj) -> {
				ItemUtils.name(inv.getItem(1), (String) obj);
				p.openInventory(inv);
			}).enterOrLeave(p);
			break;

		case 3:
			Lang.NPC_SKIN.send(p);
			Inventories.closeWithoutExit(p);
			new TextEditor(p, (obj) -> {
				if (obj != null) inv.setItem(slot, ItemUtils.skull(ItemUtils.getName(skin), (String) obj, ItemUtils.getLore(skin)));
				p.openInventory(inv);
			}).enterOrLeave(p);
			break;
			
		case 5:
			Inventories.create(p, new EntityTypeGUI((obj) -> {
					Inventories.put(p, openLastInv(p), inv);
					en = (EntityType) obj;
					if (en == EntityType.PLAYER){
						inv.setItem(5, ItemUtils.skull(Lang.name.toString(), null, "player"));
					}else inv.setItem(5, ItemUtils.item(XMaterial.mobItem(en), Lang.name.toString(), en.getName()));
			}));
			break;
			
		case 7:
			Inventories.closeAndExit(p);
			run.run(null);
			break;
			
		case 8:
			NPC npc = CitizensAPI.getNPCRegistry().createNPC(
					en,
					ItemUtils.getName(inv.getItem(1)));
			npc.data().setPersistent("player-skin-name", ItemUtils.getOwner(inv.getItem(3)));
			if (!Settings.Setting.DEFAULT_LOOK_CLOSE.asBoolean()) npc.getTrait(LookClose.class).toggle();
			if (ItemUtils.getName(inv.getItem(1)).equals(ItemUtils.getName(name))) npc.data().setPersistent("nameplate-visible", false);
			npc.spawn(p.getLocation());
			((Citizens) CitizensAPI.getPlugin()).getNPCSelector().select(p, npc);
			Inventories.closeAndExit(p);
			run.run(npc);
			break;
		
		}
		return true;
	}

}