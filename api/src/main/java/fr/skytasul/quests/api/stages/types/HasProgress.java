package fr.skytasul.quests.api.stages.types;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.PlayerAccount;

public interface HasProgress {

	int getPlayerAmount(@NotNull PlayerAccount account);

	int getTotalAmount();

}
