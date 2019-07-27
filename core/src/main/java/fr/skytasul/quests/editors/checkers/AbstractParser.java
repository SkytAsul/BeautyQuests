package fr.skytasul.quests.editors.checkers;

import org.bukkit.entity.Player;

public abstract interface AbstractParser {

	public abstract Object parse(Player p, String msg) throws Throwable;
	
}
