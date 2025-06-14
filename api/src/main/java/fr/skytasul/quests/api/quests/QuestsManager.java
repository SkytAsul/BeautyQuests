package fr.skytasul.quests.api.quests;

import fr.skytasul.quests.api.questers.Quester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.io.File;
import java.util.List;

public interface QuestsManager {

	@NotNull
	File getSaveFolder();

	@NotNull
	@UnmodifiableView
	List<@NotNull ? extends Quest> getQuests();

	@Nullable
	Quest getQuest(int id);

	void addQuest(@NotNull Quest quest);

	@NotNull
	List<@NotNull ? extends Quest> getQuestsStarted(Quester acc);

	@NotNull
	List<@NotNull ? extends Quest> getQuestsStarted(@NotNull Quester acc, boolean hide,
			boolean withoutScoreboard);

	@NotNull
	List<@NotNull ? extends Quest> getQuestsFinished(@NotNull Quester acc, boolean hide);

	@NotNull
	List<@NotNull ? extends Quest> getQuestsNotStarted(@NotNull Quester acc, boolean hide,
			boolean clickableAndRedoable);

	void updateQuestsStarted(@NotNull Quester acc, boolean withoutScoreboard, @NotNull List<@NotNull Quest> list);

	int getStartedSize(@NotNull Quester acc);

}
