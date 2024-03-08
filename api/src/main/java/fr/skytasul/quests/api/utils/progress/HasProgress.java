package fr.skytasul.quests.api.utils.progress;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.PlayerAccount;

public interface HasProgress {

	long getPlayerAmount(@NotNull PlayerAccount account);

	long getTotalAmount();

}
