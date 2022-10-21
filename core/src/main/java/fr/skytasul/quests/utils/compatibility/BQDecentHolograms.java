package fr.skytasul.quests.utils.compatibility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.AbstractHolograms;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class BQDecentHolograms extends AbstractHolograms<Hologram> {

	private int counter = Integer.MIN_VALUE + ThreadLocalRandom.current().nextInt(1000000);

	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}
	
	@Override
	public boolean supportItems() {
		return true;
	}
	
	@Override
	public DecentHologram createHologram(Location lc, boolean defaultVisible) {
		Hologram hologram = DHAPI.createHologram("BQ_holo_" + counter++, lc, false);
		hologram.setDefaultVisibleState(defaultVisible);
		hologram.enable();
		return new DecentHologram(hologram);
	}
	
	public class DecentHologram extends BQHologram {
		
		protected DecentHologram(Hologram hologram) {
			super(hologram);
		}
		
		@Override
		public void appendTextLine(String text) {
			DHAPI.addHologramLine(hologram, text);
		}
		
		@Override
		public void appendItem(ItemStack item) {
			DHAPI.addHologramLine(hologram, item);
		}

		@Override
		public void setPlayerVisibility(Player p, boolean visible) {
			if (visible) {
				hologram.setShowPlayer(p);
				hologram.show(p, 0);
			}else {
				hologram.removeShowPlayer(p);
				hologram.hide(p);
			}
		}
		
		@Override
		public void setPlayersVisible(List<Player> players) {
			List<Player> leftover = new ArrayList<>(players);
			for (Iterator<UUID> iterator = hologram.getShowPlayers().iterator(); iterator
					.hasNext();) {
				UUID visible = iterator.next();
				Player player = Bukkit.getPlayer(visible);
				if (player == null) {
					iterator.remove();
					continue;
				}

				if (leftover.remove(player)) {
					// the player should see the hologram
					// and can already see it: nothing happens
				} else {
					// the player should not see the hologram
					iterator.remove();
					hologram.hide(player);
				}
			}

			for (Player invisible : leftover) {
				hologram.setShowPlayer(invisible);
				hologram.show(invisible, 0);
			}
		}
		
		@Override
		public void teleport(Location lc) {
			DHAPI.moveHologram(hologram, lc);
		}
		
		@Override
		public void delete() {
			hologram.delete();
		}
		
	}
	
}
