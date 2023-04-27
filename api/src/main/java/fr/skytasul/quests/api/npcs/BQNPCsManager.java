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
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.internal.BQNPCClickEvent;

public abstract class BQNPCsManager implements Listener {
	
	protected final Map<Integer, BQNPC> npcs = new HashMap<>();
	
	public abstract int getTimeToWaitForNPCs();
	
	public abstract @NotNull Collection<@NotNull Integer> getIDs();
	
	public abstract boolean isNPC(@NotNull Entity entity);
	
	public final @NotNull BQNPC createNPC(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
			@Nullable String skin) {
		BQNPC npc = create(location, type, name);
		try {
			if (type == EntityType.PLAYER) npc.setSkin(skin);
		}catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to set NPC skin", ex);
		}
		npcs.put(npc.getId(), npc);
		return npc;
	}
	
	public abstract boolean isValidEntityType(@NotNull EntityType type);
	
	protected abstract @NotNull BQNPC create(@NotNull Location location, @NotNull EntityType type, @NotNull String name);
	
	public final @Nullable BQNPC getById(int id) {
		return npcs.computeIfAbsent(id, this::fetchNPC);
	}
	
	protected abstract @Nullable BQNPC fetchNPC(int id);
	
	protected final void removeEvent(int id) {
		BQNPC npc = npcs.get(id);
		if (npc == null) return;
		npc.delete("NPC #" + id + " removed");
		npcs.remove(id);
	}
	
	protected final void clickEvent(@NotNull Cancellable event, int npcID, @NotNull Player p, @NotNull ClickType click) {
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
