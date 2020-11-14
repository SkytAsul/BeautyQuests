package fr.skytasul.quests.api;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractHolograms<T> {
	
	public abstract boolean supportPerPlayerVisibility();
	
	public abstract boolean supportItems();
	
	public abstract BQHologram createHologram(Location lc, boolean defaultVisible);
	
	public abstract class BQHologram {
		protected T hologram;
		
		protected BQHologram(T hologram) {
			this.hologram = hologram;
		}
		
		public void setPlayersVisible(List<Player> players) {
			players.forEach(p -> setPlayerVisibility(p, true));
		}
		
		public void setPlayerVisibility(Player p, boolean visible) {
			throw new UnsupportedOperationException();
		}
		
		public void appendItem(ItemStack item) {
			throw new UnsupportedOperationException();
		}
		
		public abstract void appendTextLine(String text);
		
		public abstract void teleport(Location lc);
		
		public abstract void delete();
	}
	
}
