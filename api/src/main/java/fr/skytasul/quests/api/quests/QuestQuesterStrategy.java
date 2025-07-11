package fr.skytasul.quests.api.quests;

import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.QuesterProvider;
import org.jetbrains.annotations.NotNull;

public interface QuestQuesterStrategy {

	/**
	 * Get the QuesterProvider instance that is used to know the quester for which the actions in
	 * relation with the quest will be done for players.
	 * <p>
	 * The returned instance is a provider present in {@link QuesterManager#getQuesterProviders()}.
	 *
	 * @return a quester provider
	 */
	@NotNull
	QuesterProvider questerProvider();

	/**
	 * @return <code>true</code> if all online players part of the quester must match the requirements
	 *         in order for the quest to be able to start
	 */
	boolean shouldAllPlayersMatchRequirements();

}
