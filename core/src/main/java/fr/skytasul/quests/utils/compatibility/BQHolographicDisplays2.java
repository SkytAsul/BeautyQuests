package fr.skytasul.quests.utils.compatibility;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.AbstractHolograms;

public class BQHolographicDisplays2 extends AbstractHolograms<Hologram> {

	private final boolean protocolLib = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");

	private Field visibilitiesField;
	
	@Override
	public boolean supportPerPlayerVisibility() {
		return protocolLib;
	}
	
	@Override
	public boolean supportItems() {
		return true;
	}
	
	@Override
	public HD2Hologram createHologram(Location lc, boolean visible) {
		Hologram holo = HologramsAPI.createHologram(BeautyQuests.getInstance(), lc);
		if (protocolLib) holo.getVisibilityManager().setVisibleByDefault(visible);
		return new HD2Hologram(holo);
	}
	
	public class HD2Hologram extends BQHologram {
		
		protected HD2Hologram(Hologram hologram) {
			super(hologram);
		}
		
		@Override
		public void setPlayersVisible(List<Player> players) {
			try {
				List<String> all = players.stream().map(Player::getName).collect(Collectors.toList());
				VisibilityManager visibility = hologram.getVisibilityManager();
				
				if (visibilitiesField == null) {
					visibilitiesField = visibility.getClass().getDeclaredField("playersVisibilityMap");
					visibilitiesField.setAccessible(true);
				}
				Map<String, Boolean> map = (Map<String, Boolean>) visibilitiesField.get(visibility);
				if (map == null) {
					map = new ConcurrentHashMap<>();
					visibilitiesField.set(visibility, map);
				}
				
				for (Iterator<Entry<String, Boolean>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, Boolean> en = iterator.next();
					if (!en.getValue()) continue;
					if (!all.contains(en.getKey())) {
						Player player = Bukkit.getPlayer(en.getKey());
						if (player != null) {
							visibility.hideTo(player);
						}else {
							iterator.remove();
						}
					}
					all.remove(en.getKey());
				}
				for (String p : all) {
					if (p == null) continue;
					visibility.showTo(Bukkit.getPlayer(p));
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
