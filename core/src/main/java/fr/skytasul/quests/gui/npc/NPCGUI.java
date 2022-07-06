package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class NPCGUI implements CustomInventory{

	private static final ItemStack nameItem = ItemUtils.item(XMaterial.NAME_TAG, Lang.name.toString());
	private static final ItemStack move = ItemUtils.item(XMaterial.MINECART, Lang.move.toString(), Lang.moveLore.toString());
	public static ItemStack validMove = ItemUtils.item(XMaterial.EMERALD, Lang.moveItem.toString());
	
	private Consumer<BQNPC> end;
	private Runnable cancel;
	
	private Inventory inv;
	private EntityType en;
	private String name;
	private String skin;
	
	public NPCGUI(Consumer<BQNPC> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	@Override
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_NPC.toString());
		
		inv.setItem(0, move.clone());
		inv.setItem(1, nameItem.clone());
		setName("§cno name selected");
		setSkin("Knight");
		setType(EntityType.PLAYER);
		inv.setItem(7, ItemUtils.itemCancel);
		inv.setItem(8, ItemUtils.itemDone);
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void setName(String name) {
		this.name = name;
		ItemUtils.lore(inv.getItem(1), Lang.optionValue.format(name));
	}
	
	private void setType(EntityType type) {
		this.en = type;
		if (en == EntityType.PLAYER) {
			inv.setItem(5, ItemUtils.skull(Lang.type.toString(), null, Lang.optionValue.format("player")));
		}else inv.setItem(5, ItemUtils.item(Utils.mobItem(en), Lang.type.toString(), Lang.optionValue.format(en.getName())));
	}
	
	private void setSkin(String skin) {
		this.skin = skin;
		inv.setItem(3, ItemUtils.skull(Lang.skin.toString(), skin, Lang.optionValue.format(skin)));
	}

	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		switch (slot){
		
		case 0:
			new WaitClick(p, () -> openLastInv(p), validMove.clone(), () -> openLastInv(p)).enter();
			break;

		case 1:
			Lang.NPC_NAME.send(p);
			new TextEditor<String>(p, () -> openLastInv(p), obj -> {
				setName(obj);
				openLastInv(p);
			}).enter();
			break;

		case 3:
			Lang.NPC_SKIN.send(p);
			Inventories.closeWithoutExit(p);
			new TextEditor<String>(p, () -> openLastInv(p), obj -> {
				if (obj != null) setSkin(obj);
				openLastInv(p);
			}).useStrippedMessage().enter();
			break;
			
		case 5:
			Inventories.create(p, new EntityTypeGUI(en -> {
				setType(en);
				Inventories.put(p, openLastInv(p), inv);
			}, x -> x != null && QuestsAPI.getNPCsManager().isValidEntityType(x)));
			break;
			
		case 7:
			Inventories.closeAndExit(p);
			cancel.run();
			break;
			
		case 8:
			Inventories.closeAndExit(p);
			try {
				end.accept(QuestsAPI.getNPCsManager().createNPC(p.getLocation(), en, name, skin));
			}catch (Exception ex) {
				ex.printStackTrace();
				Lang.ERROR_OCCURED.send(p, "npc creation " + ex.getMessage());
				cancel.run();
			}
			break;
		
		}
		return true;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(cancel);
		return CloseBehavior.NOTHING;
	}
	
}