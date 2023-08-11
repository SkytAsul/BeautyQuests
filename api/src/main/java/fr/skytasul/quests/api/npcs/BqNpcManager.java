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

	void addInternalFactory(@NotNull String key, @NotNull BqInternalNpcFactory internalFactory);

	@NotNull
	Collection<String> getAvailableIds();

	boolean isNPC(@NotNull Entity entity);

	@NotNull
	BqNpc createNPC(@NotNull BqInternalNpcFactoryCreatable internalFactory, @NotNull Location location,
			@NotNull EntityType type, @NotNull String name, @Nullable String skin);

	@Nullable
	BqNpc getById(String id);

	void npcRemoved(@NotNull BqInternalNpcFactory internalFactory, int id);

	void npcClicked(@NotNull BqInternalNpcFactory internalFactory, @Nullable Cancellable event, int npcID, @NotNull Player p,
			@NotNull NpcClickType click);

	void reload(@NotNull BqInternalNpcFactory internalFactory);

}