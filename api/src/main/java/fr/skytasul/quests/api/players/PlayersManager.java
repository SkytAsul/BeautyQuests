package fr.skytasul.quests.api.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.QuesterProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface PlayersManager extends QuesterProvider {

	public @UnknownNullability PlayerQuester getQuester(@NotNull Player p);

	public static @UnknownNullability PlayerQuester getPlayerAccount(@NotNull Player p) {
		return QuestsPlugin.getPlugin().getPlayersManager().getQuester(p);
	}

}
