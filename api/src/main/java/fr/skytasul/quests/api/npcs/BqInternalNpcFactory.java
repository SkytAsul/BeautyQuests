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
	Collection<@NotNull String> getIDs();

	@Nullable
	BqInternalNpc fetchNPC(String id);

	default void npcClicked(@Nullable Cancellable event, String npcID, @NotNull Player p, @NotNull NpcClickType click) {
		QuestsPlugin.getPlugin().getNpcManager().npcClicked(this, event, npcID, p, click);
	}

	default void npcRemoved(String id) {
		QuestsPlugin.getPlugin().getNpcManager().npcRemoved(this, id);
	}

	default void npcsReloaded() {
		QuestsPlugin.getPlugin().getNpcManager().reload(this);
	}

	public interface BqInternalNpcFactoryCreatable extends BqInternalNpcFactory {

		boolean isValidEntityType(@NotNull EntityType type);

		@NotNull
		BqInternalNpc create(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
				@Nullable String skin);

	}

}
