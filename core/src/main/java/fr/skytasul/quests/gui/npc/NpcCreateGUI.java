package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;

public class NpcCreateGUI extends CustomInventory {

	private static final ItemStack nameItem = ItemUtils.item(XMaterial.NAME_TAG, Lang.name.toString());
	private static final ItemStack move = ItemUtils.item(XMaterial.MINECART, Lang.move.toString(), Lang.moveLore.toString());
	public static ItemStack validMove = ItemUtils.item(XMaterial.EMERALD, Lang.moveItem.toString());
	
	private Consumer<BQNPC> end;
	private Runnable cancel;
	
	private EntityType en;
	private String name;
	private String skin;
	
	public NpcCreateGUI(Consumer<BQNPC> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 9, Lang.INVENTORY_NPC.toString());
	}
	
	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(0, move.clone());
		inventory.setItem(1, nameItem.clone());
		setName("§cno name selected");
		setSkin("Knight");
		setType(EntityType.PLAYER);
		inventory.setItem(7, ItemUtils.itemCancel);
		inventory.setItem(8, ItemUtils.itemDone);
	}
	
	private void setName(String name) {
		this.name = name;
		ItemUtils.lore(getInventory().getItem(1), Lang.optionValue.format(name));
	}
	
	private void setType(EntityType type) {
		this.en = type;
		if (en == EntityType.PLAYER) {
			getInventory().setItem(5, ItemUtils.skull(Lang.type.toString(), null, Lang.optionValue.format("player")));
		} else
			getInventory().setItem(5,
					ItemUtils.item(Utils.mobItem(en), Lang.type.toString(), Lang.optionValue.format(en.getName())));
	}
	
	private void setSkin(String skin) {
		this.skin = skin;
		getInventory().setItem(3, ItemUtils.skull(Lang.skin.toString(), skin, Lang.optionValue.format(skin)));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot){
		
		case 0:
			new WaitClick(p, () -> reopen(p), validMove.clone(), () -> reopen(p)).start();
			break;

		case 1:
			Lang.NPC_NAME.send(p);
			new TextEditor<String>(p, () -> reopen(p), obj -> {
				setName(obj);
				reopen(p);
			}).start();
			break;

		case 3:
			Lang.NPC_SKIN.send(p);
			new TextEditor<String>(p, () -> reopen(p), obj -> {
				if (obj != null) setSkin(obj);
				reopen(p);
			}).useStrippedMessage().start();
			break;
			
		case 5:
			new EntityTypeGUI(en -> {
				setType(en);
				reopen(p);
			}, x -> x != null && QuestsAPI.getAPI().getNPCsManager().isValidEntityType(x)).open(p);
			break;
			
		case 7:
			close(p);
			cancel.run();
			break;
			
		case 8:
			close(p);
			try {
				end.accept(QuestsAPI.getAPI().getNPCsManager().createNPC(p.getLocation(), en, name, skin));
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
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(cancel);
	}
	
}