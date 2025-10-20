package fr.skytasul.quests.api.utils.progress;

import fr.skytasul.quests.api.questers.Quester;
import org.jetbrains.annotations.NotNull;

public interface HasProgress {

	long getRemainingAmount(@NotNull Quester quester);

	long getTotalAmount();

}
