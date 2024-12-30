package fr.skytasul.quests.api.utils.progress;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.Quester;

public interface HasProgress {

	long getRemainingAmount(@NotNull Quester quester);

	long getTotalAmount();

}
