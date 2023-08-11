package fr.skytasul.quests.npcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.internal.BQNPCClickEvent;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.BqNpcManager;
import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.utils.QuestUtils;

public class BqNpcManagerImplementation implements BqNpcManager {
	
	private static final String SEPARATOR = "#";

	private final BiMap<String, BqInternalNpcFactory> internalFactories = HashBiMap.create();
	private final Map<String, BqNpcImplementation> npcs = new HashMap<>();
	
	private BqInternalNpcFactory last = null;

	public @NotNull String getFactoryKey(@NotNull BqInternalNpcFactory internalFactory) {
		return internalFactories.inverse().get(internalFactory);
	}

	private String getNpcId(BqInternalNpcFactory factory, int id) {
		return getFactoryKey(factory) + SEPARATOR + id;
	}

	private BqInternalNpcFactory getMigrationFactory() {
		return last;
	}

	public Collection<BqInternalNpcFactory> getInternalFactories() {
		return internalFactories.values();
	}

	public long getTimeToWaitForNPCs() {
		return internalFactories.values().stream().mapToInt(BqInternalNpcFactory::getTimeToWaitForNPCs).max().orElse(0);
	}

	@Override
	public boolean isEnabled() {
		return !internalFactories.isEmpty();
	}

	@Override
	public void addInternalFactory(@NotNull String key, @NotNull BqInternalNpcFactory internalFactory) {
		if (internalFactories.containsKey(key))
			throw new IllegalArgumentException("Npc factory " + key + " is already registered");

		QuestsPlugin.getPlugin().getLoggerExpanded().info("Adding " + key + " as a npc factory");
		internalFactories.put(key, internalFactory);

		last = internalFactory;

		QuestUtils.autoRegister(internalFactory);
	}

	@Override
	public @NotNull Collection<String> getAvailableIds() {
		return internalFactories.values()
				.stream()
				.flatMap(factory -> factory.getIDs()
						.stream()
						.map(String::valueOf))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isNPC(@NotNull Entity entity) {
		return internalFactories.values().stream().anyMatch(factory -> factory.isNPC(entity));
	}

	@Override
	public @NotNull BqNpc createNPC(@NotNull BqInternalNpcFactoryCreatable internalFactory, @NotNull Location location,
			@NotNull EntityType type, @NotNull String name, @Nullable String skin) {
		BqInternalNpc internal = internalFactory.create(location, type, name, skin);

		BqNpcImplementation npc = new BqNpcImplementation(new WrappedInternalNpc(internalFactory, internal));
		npcs.put(npc.getId(), npc);
		return npc;
	}
	
	@Override
	public @Nullable BqNpcImplementation getById(String id) {
		return npcs.computeIfAbsent(id, this::registerNPC);
	}

	public @Nullable BqNpcImplementation getById(BqInternalNpcFactory npcFactory, int id) {
		return npcs.computeIfAbsent(getNpcId(npcFactory, id), this::registerNPC);
	}

	private BqNpcImplementation registerNPC(String id) {
		BqInternalNpcFactory factory;
		int npcId;

		int separatorIndex = id.indexOf(SEPARATOR);
		if (separatorIndex == -1) { // TODO remove, migration in 1.0
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("Loading NPC with id " + id + " from a previous version of the plugin.");
			factory = getMigrationFactory();
			npcId = Integer.parseInt(id);
		} else {
			String factoryKey = id.substring(0, separatorIndex);
			factory = internalFactories.get(factoryKey);
			npcId = Integer.parseInt(id.substring(separatorIndex + SEPARATOR.length()));
		}

		return new BqNpcImplementation(new WrappedInternalNpc(factory, npcId));
	}
	
	@Override
	public void npcRemoved(BqInternalNpcFactory npcFactory, int id) {
		String npcId = getNpcId(npcFactory, id);
		BqNpcImplementation npc = npcs.get(npcId);
		if (npc == null) return;
		npc.delete("NPC " + npcId + " removed");
		npcs.remove(npcId);
	}
	
	@Override
	public void npcClicked(BqInternalNpcFactory npcFactory, @Nullable Cancellable event, int npcID, @NotNull Player p,
			@NotNull NpcClickType click) {
		if (event != null && event.isCancelled())
			return;
		BQNPCClickEvent newEvent = new BQNPCClickEvent(getById(npcFactory, npcID), p, click);
		Bukkit.getPluginManager().callEvent(newEvent);
		if (event != null)
			event.setCancelled(newEvent.isCancelled());
	}
	
	@Override
	public void reload(BqInternalNpcFactory npcFactory) {
		npcs.forEach((id, npc) -> {
			if (npc.getWrappedNpc().factory != npcFactory)
				return;
			BqInternalNpc newInternal = npcFactory.fetchNPC(npc.getWrappedNpc().id);
			if (newInternal == null) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Unable to find NPC with ID " + id + " after a NPCs manager reload.");
			} else {
				npc.getWrappedNpc().npc = newInternal;
			}
		});
	}

	public void unload() {
		npcs.values().forEach(BqNpcImplementation::unload);
		npcs.clear();
	}
	
	class WrappedInternalNpc {

		private final BqInternalNpcFactory factory;
		private final int id;
		private BqInternalNpc npc;

		public WrappedInternalNpc(BqInternalNpcFactory factory, int id) {
			this.factory = factory;
			this.id = id;
			this.npc = factory.fetchNPC(id);
		}

		public WrappedInternalNpc(BqInternalNpcFactory factory, BqInternalNpc npc) {
			this.factory = factory;
			this.npc = npc;
			this.id = npc.getInternalId();
		}

		public @NotNull String getId() {
			return getNpcId(factory, id);
		}

		public @NotNull BqInternalNpc getNpc() {
			return npc;
		}

	}

}
