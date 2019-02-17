package fr.skytasul.quests.utils.compatibility;

import java.util.OptionalInt;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class McCombatLevel {

	private static com.gmail.mrphpfan.mccombatlevel.McCombatLevel plugin = (com.gmail.mrphpfan.mccombatlevel.McCombatLevel) Bukkit.getPluginManager().getPlugin("McCombatLevel");
	
	public static int getCombatLevel(Player p){
		OptionalInt lvl = plugin.getLevel(p);
		return lvl.isPresent() ? lvl.getAsInt() : 0;
	}
	
}
