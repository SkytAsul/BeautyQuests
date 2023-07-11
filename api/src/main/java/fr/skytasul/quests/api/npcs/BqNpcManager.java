package fr.skytasul.quests.api.npcs;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BqNpcManager {

	boolean isEnabled();

	void setInternalFactory(BqInternalNpcFactory internalFactory);

	@NotNull
	BqNpc createNPC(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
			@Nullable String skin);

	@Nullable
	BqNpc getById(int id);

	void npcRemoved(int id);

	void npcClicked(@Nullable Cancellable event, int npcID, @NotNull Player p, @NotNull NpcClickType click);

	void reload();

}
