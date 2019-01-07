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

public class HolographicDisplays {

	public static boolean hasProtocolLib() {
		return com.gmail.filoghost.holographicdisplays.HolographicDisplays.hasProtocolLibHook();
	}
	
	public static Object createHologram(Location lc, boolean visible){
		Hologram holo = HologramsAPI.createHologram(BeautyQuests.getInstance(), lc);
		holo.getVisibilityManager().setVisibleByDefault(visible);
		return holo;
	}
	
	public static void setPlayersVisible(Object hologram, List<Player> players) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if (!check(hologram)) return;
		if (!hasProtocolLib()) return;
		List<Player> all = new ArrayList<>(players);
		VisibilityManager visibility = ((Hologram) hologram).getVisibilityManager();
		
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
	}
	
	public static void setPlayerVisibility(Object hologram, Player p, boolean visible) {
		if (!check(hologram)) return;
		if (visible) {
			((Hologram) hologram).getVisibilityManager().showTo(p);
		}else ((Hologram) hologram).getVisibilityManager().hideTo(p);
	}
	
	public static void appendItem(Object hologram, ItemStack item) {
		if (!check(hologram)) return;
		((Hologram) hologram).appendItemLine(item);
	}
	
	public static void appendTextLine(Object hologram, String text){
		if (!check(hologram)) return;
		((Hologram) hologram).appendTextLine(text);
	}
	
	public static void teleport(Object hologram, Location lc){
		if (!check(hologram)) return;
		((Hologram) hologram).teleport(lc);
	}
	
	public static void delete(Object hologram){
		if (!check(hologram)) return;
		((Hologram) hologram).delete();
	}
	
	private static boolean check(Object hologram){
		if (hologram == null) return false;
		if (!(hologram instanceof Hologram)) throw new IllegalArgumentException("Error in HolographicDisplays dependency : object is not an Hologram.");
		return true;
	}
	
}
