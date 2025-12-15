package fr.skytasul.quests.integrations.holograms;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.ItemHologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.data.property.Visibility;
import de.oliver.fancyholograms.api.hologram.Hologram;
import fr.skytasul.quests.api.holograms.BqHologram;
import fr.skytasul.quests.api.holograms.BqHologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BQFancyHolograms implements BqHologramManager {

	private int counter = Integer.MIN_VALUE + ThreadLocalRandom.current().nextInt(1000000);
	private HologramManager hologramsManager = FancyHologramsPlugin.get().getHologramManager();

	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}

	@Override
	public boolean supportItems() {
		return true;
	}

	@Override
	public BqFancyHologram createHologram(Location lc, boolean defaultVisible) {
		return new BqFancyHologram(lc, defaultVisible);
	}

	private class BqFancyHologram implements BqHologram {

		private final int id = counter++;

		private final List<Hologram> internalHolograms = new ArrayList<>(1);

		private Location lc;
		private boolean defaultVisible;

		public BqFancyHologram(Location lc, boolean defaultVisible) {
			this.lc = lc;
			this.defaultVisible = defaultVisible;
		}

		@Override
		public void appendTextLine(String text) {
			var data = new TextHologramData("bq-%d-%d".formatted(id, internalHolograms.size()), lc);
			data.setText(List.of(text));
			data.setVisibility(defaultVisible ? Visibility.ALL : Visibility.MANUAL);
			data.setPersistent(false);

			var holo = hologramsManager.create(data);
			hologramsManager.addHologram(holo);

			internalHolograms.add(holo);
			teleport(lc);
		}

		@Override
		public void appendItem(ItemStack item) {
			var data = new ItemHologramData("bq-%d-%d".formatted(id, internalHolograms.size()), lc);
			data.setItemStack(item);
			data.setVisibility(defaultVisible ? Visibility.ALL : Visibility.MANUAL);
			data.setPersistent(false);
			data.setScale(new Vector3f(0.25f));

			var holo = hologramsManager.create(data);
			hologramsManager.addHologram(holo);

			internalHolograms.add(holo);
			teleport(lc);
		}

		@Override
		public void setPlayerVisibility(Player p, boolean visible) {
			for (var holo : internalHolograms) {
				if (visible)
					holo.forceShowHologram(p);
				else
					holo.forceHideHologram(p);
			}
		}

		@Override
		public void setPlayersVisible(List<Player> players) {
			for (var hologram : internalHolograms) {
				// Since we have no way to get the actual distant viewers, we
				// start by simply clearing all of them.
				Visibility.ManualVisibility.remove(hologram);

				List<Player> leftover = new ArrayList<>(players);
				for (UUID visible : hologram.getViewers()) {
					Player player = Bukkit.getPlayer(visible);
					if (player == null)
						continue;

					if (leftover.remove(player)) {
						// the player should see the hologram
						// and can already see it: nothing happens
					} else {
						// the player should not see the hologram
						hologram.forceHideHologram(player);
					}
				}

				for (Player invisible : leftover) {
					hologram.forceShowHologram(invisible);
				}
			}
		}

		@Override
		public void teleport(Location lc) {
			this.lc = lc;

			for (int i = 0; i < internalHolograms.size(); i++) {
				var holo = internalHolograms.get(i);
				holo.getData().setLocation(lc.clone().subtract(0, 0.25 + 0.3 * i, 0));
			}
		}

		@Override
		public void delete() {
			internalHolograms.forEach(hologramsManager::removeHologram);
		}

	}

}
