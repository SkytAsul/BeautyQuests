package fr.skytasul.quests.api.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface BqInternalHologram {

	void appendTextLine(@Nullable String text);

	void teleport(@NotNull Location lc);

	void delete();

	public interface BqInternalHologramWithVisibility extends BqInternalHologram {

		// TODO maybe delete this method?
		@Deprecated
		void setPlayersVisible(@NotNull List<Player> players);

		void setPlayerVisibility(@NotNull Player p, boolean visible);

	}

	public interface BqInternalHologramWithItems extends BqInternalHologram {

		void appendItem(@NotNull ItemStack item);

	}

}
