package fr.skytasul.quests.api.stages.options;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public interface StageQuesterStrategy {

	/**
	 * Get questers for which the stage should be updated when the player does the action associated
	 * with the stage.
	 *
	 * @param player player that did the action
	 * @return a collection containing the questers that should progress at the same time as the
	 *         player's quester
	 */
	@NotNull
	Collection<? extends Quester> getAdditionalQuesters(@NotNull Player player);

}
