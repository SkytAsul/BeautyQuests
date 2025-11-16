package fr.skytasul.quests.players;

import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.questers.data.QuesterData;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import org.jetbrains.annotations.NotNull;

// TODO extract common things from implementations
public abstract class AbstractPlayerQuesterImplementation extends AbstractQuesterImplementation implements PlayerQuester {

	protected AbstractPlayerQuesterImplementation(@NotNull PlayerManager provider,
			@NotNull QuesterData dataHandler) {
		super(provider, dataHandler);
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);

		// we do not want the " player" prefix at the beginning if the quester is actually a player
		placeholders.register("player", this::getFriendlyName);
	}

}
