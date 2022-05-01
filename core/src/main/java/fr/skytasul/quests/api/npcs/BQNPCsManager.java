package fr.skytasul.quests.api.npcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import fr.skytasul.quests.QuestsConfiguration.ClickType;
import fr.skytasul.quests.api.events.BQNPCClickEvent;

public abstract class BQNPCsManager implements Listener {
	
	protected final Map<Integer, BQNPC> npcs = new HashMap<>();
	
	public abstract int getTimeToWaitForNPCs();
	
	public abstract Collection<Integer> getIDs();
	
	public abstract boolean isNPC(Entity entity);
	
	public final BQNPC createNPC(Location location, EntityType type, String name, String skin) {
		BQNPC npc = create(location, type, name);
		try {
			if (type == EntityType.PLAYER) npc.setSkin(skin);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		npcs.put(npc.getId(), npc);
		return npc;
	}
	
	public abstract boolean isValidEntityType(EntityType type);
	
	protected abstract BQNPC create(Location location, EntityType type, String name);
	
	public final BQNPC getById(int id) {
		return npcs.computeIfAbsent(id, this::fetchNPC);
	}
	
	protected abstract BQNPC fetchNPC(int id);
	
	protected final void removeEvent(int id) {
		BQNPC npc = npcs.get(id);
		if (npc == null) return;
		npc.delete("NPC #" + id + " removed");
		npcs.remove(id);
	}
	
	protected final void clickEvent(Cancellable event, int npcID, Player p, ClickType click) {
		if (event != null && event.isCancelled()) return;
		BQNPCClickEvent newEvent = new BQNPCClickEvent(getById(npcID), p, click);
		Bukkit.getPluginManager().callEvent(newEvent);
		if (event != null) event.setCancelled(newEvent.isCancelled());
	}
	
	public void unload() {
		npcs.values().forEach(BQNPC::unload);
		npcs.clear();
	}
	
}
