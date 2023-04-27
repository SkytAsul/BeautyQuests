package fr.skytasul.quests.api;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHolograms<T> {
	
	public abstract boolean supportPerPlayerVisibility();
	
	public abstract boolean supportItems();
	
	public abstract @NotNull BQHologram createHologram(@NotNull Location lc, boolean defaultVisible);
	
	public abstract class BQHologram {
		protected @NotNull T hologram;
		
		protected BQHologram(@NotNull T hologram) {
			this.hologram = hologram;
		}
		
		public void setPlayersVisible(@NotNull List<Player> players) {
			players.forEach(p -> setPlayerVisibility(p, true));
		}
		
		public void setPlayerVisibility(@NotNull Player p, boolean visible) {
			throw new UnsupportedOperationException();
		}
		
		public void appendItem(@NotNull ItemStack item) {
			throw new UnsupportedOperationException();
		}
		
		public abstract void appendTextLine(@Nullable String text);
		
		public abstract void teleport(@NotNull Location lc);
		
		public abstract void delete();
	}
	
}
