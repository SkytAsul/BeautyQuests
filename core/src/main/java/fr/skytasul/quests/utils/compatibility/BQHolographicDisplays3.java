package fr.skytasul.quests.utils.compatibility;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.AbstractHolograms;

import me.filoghost.holographicdisplays.api.beta.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import me.filoghost.holographicdisplays.api.beta.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.beta.hologram.VisibilitySettings.Visibility;

public class BQHolographicDisplays3 extends AbstractHolograms<Hologram> {

	private final HolographicDisplaysAPI api = HolographicDisplaysAPI.get(Bukkit.getPluginManager().getPlugin("HolographicDisplays"));
	
	private Field visibilitiesField;
	
	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}
	
	@Override
	public boolean supportItems() {
		return true;
	}
	
	@Override
	public HD3Hologram createHologram(Location lc, boolean visible) {
		Hologram holo = api.createHologram(lc);
		holo.getVisibilitySettings().setGlobalVisibility(booleanToVisibility(visible));
		return new HD3Hologram(holo);
	}
	
	private Visibility booleanToVisibility(boolean visible) {
		return visible ? Visibility.VISIBLE : Visibility.HIDDEN;
	}
	
	public class HD3Hologram extends BQHologram {
		
		protected HD3Hologram(Hologram hologram) {
			super(hologram);
		}
		
		@Override
		public void setPlayersVisible(List<Player> players) {
			try {
				List<UUID> all = players.stream().map(Player::getUniqueId).collect(Collectors.toList());
				VisibilitySettings visibility = hologram.getVisibilitySettings();
				
				if (visibilitiesField == null) {
					visibilitiesField = visibility.getClass().getDeclaredField("individualVisibilities");
					visibilitiesField.setAccessible(true);
				}
				Map<UUID, Visibility> map = (Map<UUID, Visibility>) visibilitiesField.get(visibility);
				if (map == null) {
					map = new ConcurrentHashMap<>();
					visibilitiesField.set(visibility, map);
				}
				
				for (Entry<UUID, Visibility> en : map.entrySet()) {
					if (all.remove(en.getKey())) {
						if (en.getValue() == Visibility.HIDDEN) en.setValue(Visibility.VISIBLE);
					}else {
						if (en.getValue() == Visibility.VISIBLE) en.setValue(Visibility.HIDDEN);
					}
				}
				for (UUID left : all) {
					map.put(left, Visibility.VISIBLE);
				}
			}catch (ReflectiveOperationException ex) {
				ex.printStackTrace();
			}
		}
		
		@Override
		public void setPlayerVisibility(Player p, boolean visible) {
			hologram.getVisibilitySettings().setIndividualVisibility(p, booleanToVisibility(visible));
		}
		
		@Override
		public void appendItem(ItemStack item) {
			hologram.getLines().appendItem(item);
		}
		
		@Override
		public void appendTextLine(String text) {
			hologram.getLines().appendText(text);
		}
		
		@Override
		public void teleport(Location lc) {
			hologram.setPosition(lc);
		}
		
		@Override
		public void delete() {
			hologram.delete();
		}
		
	}
	
}
