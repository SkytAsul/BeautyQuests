package fr.skytasul.quests.api.quests.quester;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.serializable.SerializableRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public abstract class QuestQuesterStrategy extends SerializableObject {

	protected QuestQuesterStrategy(@NotNull SerializableRegistry registry) {
		super(registry);
	}

	/**
	 * Gets the quester associated with the actions done in quests with this strategy.
	 *
	 * @param player
	 * @return a quester if the player can do quests according to this strategy, or an empty optional
	 *         otherwise
	 */
	@NotNull
	public abstract Optional<? extends Quester> getPlayerQuester(@NotNull Player player);

	/**
	 * Tests whether a quester is applicable according to this strategy
	 *
	 * @param quester
	 * @return <code>true</code> if the quester can be returned by {@link #getPlayerQuester(Player)}
	 */
	public abstract boolean isQuesterApplicable(@NotNull Quester quester);

	/**
	 * @return <code>true</code> if all online players part of the quester must match the requirements
	 *         in order for the quest to be able to start
	 */
	public abstract boolean shouldAllPlayersMatchRequirements();

}
