package fr.skytasul.quests.players;

import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import org.jetbrains.annotations.NotNull;

// TODO extract common things from implementations
public abstract class AbstractPlayerQuesterImplementation extends AbstractQuesterImplementation implements PlayerQuester {

	protected AbstractPlayerQuesterImplementation(@NotNull PlayersManager provider,
			@NotNull QuesterData dataHandler) {
		super(provider, dataHandler);
	}

}
