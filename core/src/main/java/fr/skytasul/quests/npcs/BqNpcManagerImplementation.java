package fr.skytasul.quests.npcs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.skytasul.quests.DefaultQuestFeatures;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.*;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BqNpcManagerImplementation implements BqNpcManager {

	private static final String SEPARATOR = "#";
	private static final Pattern FACTORY_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_-]*");

	private final BiMap<String, BqInternalNpcFactory> internalFactories = HashBiMap.create();
	private final Map<String, BqNpcImplementation> npcs = new HashMap<>();

	private BqInternalNpcFactory last = null;

	public @NotNull String getFactoryKey(@NotNull BqInternalNpcFactory internalFactory) {
		return internalFactories.inverse().get(internalFactory);
	}

	private String getNpcId(BqInternalNpcFactory factory, String internalId) {
		return getFactoryKey(factory) + SEPARATOR + internalId;
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
		if (!FACTORY_KEY_PATTERN.matcher(key).matches())
			throw new IllegalArgumentException("Invalid factory key " + key);

		QuestsPlugin.getPlugin().getLoggerExpanded().info("Adding " + key + " as a npc factory");
		internalFactories.put(key, internalFactory);

		last = internalFactory;

		QuestUtils.autoRegister(internalFactory);

		DefaultQuestFeatures.registerNpcFeatures();
	}

	@Override
	public @NotNull Collection<String> getAvailableIds() {
		return internalFactories.values()
				.stream()
				.flatMap(factory -> factory.getIDs()
						.stream()
						.map(id -> getNpcId(factory, id)))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isNPC(@NotNull Entity entity) {
		if (!entity.getMetadata("NPC").isEmpty())
			return true;
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
		BqInternalNpcFactory factory;
		String internalId;

		int separatorIndex = id.indexOf(SEPARATOR);
		if (separatorIndex == -1) { // TODO migration 1.0
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.debug("Loading NPC with id " + id + " from a previous version of the plugin.");
			factory = getMigrationFactory();
			internalId = id;
		} else {
			String factoryKey = id.substring(0, separatorIndex);
			factory = internalFactories.get(factoryKey);
			internalId = id.substring(separatorIndex + SEPARATOR.length());
		}

		if (factory == null)
			throw new IllegalArgumentException("Cannot find factory for NPC " + id + ". Is your NPC plugin installed?");

		return getByFactoryAndId(factory, internalId);
	}

	public @Nullable BqNpcImplementation getByFactoryAndId(@NotNull BqInternalNpcFactory factory, String internalId) {
		return npcs.computeIfAbsent(getNpcId(factory, internalId), strId -> {
			BqInternalNpc npc = factory.fetchNPC(internalId);
			if (npc == null)
				return null;

			return new BqNpcImplementation(new WrappedInternalNpc(factory, npc));
		});
	}

	@Override
	public void npcRemoved(BqInternalNpcFactory npcFactory, String internalId) {
		String npcId = getNpcId(npcFactory, internalId);
		BqNpcImplementation npc = npcs.get(npcId);
		if (npc == null)
			return;
		npc.delete("NPC " + npcId + " removed");
		npcs.remove(npcId);
	}

	@Override
	public void npcClicked(BqInternalNpcFactory npcFactory, @Nullable Cancellable event, String internalId,
			@NotNull Player p, @NotNull NpcClickType click) {
		if (event != null && event.isCancelled())
			return;

		BQNPCClickEvent newEvent = new BQNPCClickEvent(getByFactoryAndId(npcFactory, internalId), p, click);
		if (event == null)
			QuestUtils.runOrSync(() -> Bukkit.getPluginManager().callEvent(newEvent));
		else
			QuestUtils.tunnelEventCancelling(event, newEvent);
	}

	@Override
	public void reload(BqInternalNpcFactory npcFactory) {
		npcs.forEach((id, npc) -> {
			if (npc.getWrappedNpc().factory != npcFactory)
				return;
			BqInternalNpc newInternal = npcFactory.fetchNPC(npc.getWrappedNpc().internalId);
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
		private final String internalId;
		private BqInternalNpc npc;

		public WrappedInternalNpc(BqInternalNpcFactory factory, BqInternalNpc npc) {
			this.factory = factory;
			this.npc = npc;
			this.internalId = npc.getInternalId();
		}

		public @NotNull String getId() {
			return getNpcId(factory, internalId);
		}

		public @NotNull BqInternalNpc getNpc() {
			return npc;
		}

	}

}
