package fr.skytasul.quests.api.editors.parsers;

import org.bukkit.entity.Player;

public abstract interface AbstractParser<T> {

	public abstract T parse(Player p, String msg) throws Throwable;
	
	public default void sendIndication(Player p) {}
	
}
