package fr.skytasul.quests.editors.checkers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldParser implements AbstractParser {

	public Object parse(Player p, String msg) throws Throwable {
		World world = Bukkit.getWorld(msg);
		if (world == null) p.sendMessage("§cThis world does not exist.");
		return world;
	}

}
