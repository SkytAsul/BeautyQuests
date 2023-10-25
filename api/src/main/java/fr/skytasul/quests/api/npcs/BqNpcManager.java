package fr.skytasul.quests.api.npcs;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;

public interface BqNpcManager {

	boolean isEnabled();

	/**
	 * Adds a new npc factory to the npc manager.
	 *
	 * @param key unique key of the npc factory
	 * @param internalFactory factory
	 */
	void addInternalFactory(@NotNull String key, @NotNull BqInternalNpcFactory internalFactory);

	@NotNull
	Collection<String> getAvailableIds();

	boolean isNPC(@NotNull Entity entity);

	@NotNull
	BqNpc createNPC(@NotNull BqInternalNpcFactoryCreatable internalFactory, @NotNull Location location,
			@NotNull EntityType type, @NotNull String name, @Nullable String skin);

	@Nullable
	BqNpc getById(String id);

	void npcRemoved(@NotNull BqInternalNpcFactory internalFactory, String internalId);

	void npcClicked(@NotNull BqInternalNpcFactory internalFactory, @Nullable Cancellable event, String internalId,
			@NotNull Player p, @NotNull NpcClickType click);

	void reload(@NotNull BqInternalNpcFactory internalFactory);

}
