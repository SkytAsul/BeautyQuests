package fr.skytasul.quests.api.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.QuesterProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.List;

public interface PlayersManager extends QuesterProvider {

	@Override
	@NotNull
	@UnmodifiableView
	Collection<? extends PlayerQuester> getLoadedQuesters();

	@Override
	default @NotNull @UnmodifiableView Collection<? extends PlayerQuester> getPlayerQuesters(@NotNull Player player) {
		var quester = getQuester(player);
		return quester == null ? List.of() : List.of(quester);
	}

	public @UnknownNullability PlayerQuester getQuester(@NotNull Player p);

	public static @UnknownNullability PlayerQuester getPlayerAccount(@NotNull Player p) {
		return QuestsPlugin.getPlugin().getPlayersManager().getQuester(p);
	}

}
