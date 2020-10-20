package fr.skytasul.quests.utils.compatibility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.AbstractHolograms;

public class BQHolographicDisplays extends AbstractHolograms<Hologram> {

	private final boolean protocolLib = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");

	@Override
	public boolean supportPerPlayerVisibility() {
		return protocolLib;
	}
	
	@Override
	public boolean supportItems() {
		return true;
	}
	
	@Override
	public HDHologram createHologram(Location lc, boolean visible) {
		Hologram holo = HologramsAPI.createHologram(BeautyQuests.getInstance(), lc);
		if (protocolLib) holo.getVisibilityManager().setVisibleByDefault(visible);
		return new HDHologram(holo);
	}
	
	public class HDHologram extends BQHologram {
		
		protected HDHologram(Hologram hologram) {
			super(hologram);
		}
		
		@Override
		public void setPlayersVisible(List<Player> players) {
			try {
				List<Player> all = new ArrayList<>(players);
				VisibilityManager visibility = hologram.getVisibilityManager();
				
				Field field = visibility.getClass().getDeclaredField("playersVisibilityMap");
				field.setAccessible(true);
				Map<String, Boolean> map = (Map<String, Boolean>) field.get(visibility);
				if (map == null) field.set(visibility, new ConcurrentHashMap<>());
				map = (Map<String, Boolean>) field.get(visibility);
				
				for (Entry<String, Boolean> en : map.entrySet()) {
					if (!en.getValue()) continue;
					Player p = Bukkit.getPlayer(en.getKey());
					if (p == null) continue;
					if (!all.contains(p)) {
						visibility.hideTo(p);
					}
					all.remove(p);
				}
				for (Player p : all) {
					if (p == null) continue;
					visibility.showTo(p);
				}
			}catch (ReflectiveOperationException ex) {
				ex.printStackTrace();
			}
		}
		
		@Override
		public void setPlayerVisibility(Player p, boolean visible) {
			if (visible) {
				hologram.getVisibilityManager().showTo(p);
			}else hologram.getVisibilityManager().hideTo(p);
		}
		
		@Override
		public void appendItem(ItemStack item) {
			hologram.appendItemLine(item);
		}
		
		@Override
		public void appendTextLine(String text) {
			hologram.appendTextLine(text);
		}
		
		@Override
		public void teleport(Location lc) {
			hologram.teleport(lc);
		}
		
		@Override
		public void delete() {
			hologram.delete();
		}
		
	}
	
}
