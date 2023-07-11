package fr.skytasul.quests.npcs;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.internal.BQNPCClickEvent;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.BqNpcManager;
import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.utils.QuestUtils;

public class BqNpcManagerImplementation implements BqNpcManager {
	
	private final Map<Integer, BqNpcImplementation> npcs = new HashMap<>();
	private @Nullable BqInternalNpcFactory internalFactory = null;
	
	@Override
	public boolean isEnabled() {
		return internalFactory != null;
	}

	public @Nullable BqInternalNpcFactory getInternalFactory() {
		return internalFactory;
	}

	@Override
	public void setInternalFactory(BqInternalNpcFactory internalFactory) {
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.info(internalFactory.getClass().getSimpleName() + " will replace "
						+ (this.internalFactory == null ? "none" : this.internalFactory.getClass().getSimpleName())
						+ " as the new NPCs manager.");

		this.internalFactory = internalFactory;

		QuestUtils.autoRegister(internalFactory);
	}

	@Override
	public @NotNull BqNpc createNPC(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
			@Nullable String skin) {
		BqInternalNpc internal = internalFactory.create(location, type, name);
		try {
			if (type == EntityType.PLAYER)
				internal.setSkin(skin);
		}catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to set NPC skin", ex);
		}

		BqNpcImplementation npc = new BqNpcImplementation(internal);
		npcs.put(internal.getId(), npc);
		return npc;
	}
	
	@Override
	public @Nullable BqNpcImplementation getById(int id) {
		return npcs.computeIfAbsent(id, this::registerNPC);
	}

	private BqNpcImplementation registerNPC(int id) {
		return new BqNpcImplementation(internalFactory.fetchNPC(id));
	}
	
	@Override
	public void npcRemoved(int id) {
		BqNpcImplementation npc = npcs.get(id);
		if (npc == null) return;
		npc.delete("NPC #" + id + " removed");
		npcs.remove(id);
	}
	
	@Override
	public void npcClicked(@Nullable Cancellable event, int npcID, @NotNull Player p, @NotNull NpcClickType click) {
		if (event != null && event.isCancelled())
			return;
		BQNPCClickEvent newEvent = new BQNPCClickEvent(getById(npcID), p, click);
		Bukkit.getPluginManager().callEvent(newEvent);
		if (event != null)
			event.setCancelled(newEvent.isCancelled());
	}
	
	@Override
	public void reload() {
		npcs.forEach((id, npc) -> {
			BqInternalNpc newInternal = internalFactory.fetchNPC(id);
			if (newInternal == null) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Unable to find NPC with ID " + id + " after a NPCs manager reload.");
			} else {
				npc.setNpc(newInternal);
			}
		});
	}

	public void unload() {
		npcs.values().forEach(BqNpcImplementation::unload);
		npcs.clear();
	}
	
}
