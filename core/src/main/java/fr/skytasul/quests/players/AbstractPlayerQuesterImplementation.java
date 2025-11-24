package fr.skytasul.quests.players;

import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.questers.data.QuesterData;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO extract common things from implementations
public abstract class AbstractPlayerQuesterImplementation extends AbstractQuesterImplementation implements PlayerQuester {

	protected AbstractPlayerQuesterImplementation(@NotNull PlayerManager provider,
			@NotNull QuesterData dataHandler) {
		super(provider, dataHandler);
	}

	@Override
	public @NotNull String getDisplayName() {
		return getPlayer().map(Player::getDisplayName).orElseGet(this::getFriendlyName);
	}

}
