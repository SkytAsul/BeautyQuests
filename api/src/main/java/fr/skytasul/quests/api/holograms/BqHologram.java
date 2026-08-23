package fr.skytasul.quests.api.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface BqHologram {

	default void setPlayersVisible(@NotNull List<Player> players) {
		throw new UnsupportedOperationException();
	}

	default void setPlayerVisibility(@NotNull Player p, boolean visible) {
		throw new UnsupportedOperationException();
	}

	default void appendItem(@NotNull ItemStack item) {
		throw new UnsupportedOperationException();
	}

	void appendTextLine(@Nullable String text);

	void teleport(@NotNull Location lc);

	void delete();

}
