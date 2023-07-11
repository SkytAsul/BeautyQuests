package fr.skytasul.quests.api.npcs;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.AutoRegistered;

@AutoRegistered
public interface BqInternalNpcFactory {

	int getTimeToWaitForNPCs();

	boolean isNPC(@NotNull Entity entity);

	@NotNull
	Collection<@NotNull Integer> getIDs();

	@Nullable
	BqInternalNpc fetchNPC(int id);

	@NotNull
	BqInternalNpc create(@NotNull Location location, @NotNull EntityType type, @NotNull String name);

	boolean isValidEntityType(@NotNull EntityType type);

	default void npcClicked(@Nullable Cancellable event, int npcID, @NotNull Player p, @NotNull NpcClickType click) {
		QuestsPlugin.getPlugin().getNpcManager().npcClicked(event, npcID, p, click);
	}

	default void npcRemoved(int id) {
		QuestsPlugin.getPlugin().getNpcManager().npcRemoved(id);
	}

	default void npcsReloaded() {
		QuestsPlugin.getPlugin().getNpcManager().reload();
	}

}
