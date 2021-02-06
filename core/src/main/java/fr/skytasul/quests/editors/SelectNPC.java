package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.QuestsConfiguration.ClickType;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class SelectNPC extends InventoryClear{
	
	private Consumer<NPC> run;
	
	public SelectNPC(Player p, Runnable cancel, Consumer<NPC> end) {
		super(p, cancel);
		this.run = end;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (QuestsConfiguration.getNPCClick().applies(ClickType.RIGHT)) onNPCClick(e);
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		if (QuestsConfiguration.getNPCClick().applies(ClickType.LEFT)) onNPCClick(e);
	}
	
	private void onNPCClick(NPCClickEvent e) {
		if (e.getClicker() != p) return;
		e.setCancelled(true);
		leave(e.getClicker());
		run.accept(e.getNPC());
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onClick(PlayerInteractEvent e) {
		if (e.getPlayer() != p) return;
		if (ItemUtils.itemCancel.equals(e.getItem())) {
			e.setCancelled(true);
			cancel();
		}
	}

	protected String cancelWord(){
		return "cancel";
	}
	
	public void begin(){
		super.begin();
		Utils.sendMessage(p, Lang.NPC_EDITOR_ENTER.toString());
		if (cancel != null) p.getInventory().setItem(8, ItemUtils.itemCancel);
	}

}