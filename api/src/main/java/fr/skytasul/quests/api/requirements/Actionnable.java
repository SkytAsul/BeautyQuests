package fr.skytasul.quests.api.requirements;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Actionnable {

	public void trigger(@NotNull Player p);

}
