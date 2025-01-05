package fr.skytasul.quests.api.questers;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.pools.QuestPool;

public interface QuesterPoolData {

	@NotNull
	Quester getQuester();

	int getPoolID();

	@Nullable
	QuestPool getPool();

	long getLastGive();

	void setLastGive(long lastGive);

	@NotNull
	@UnmodifiableView
	Set<@NotNull Integer> getCompletedQuests();

	void setCompletedQuests(@NotNull Set<Integer> completedQuests);

}
