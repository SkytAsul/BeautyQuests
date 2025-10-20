package fr.skytasul.quests.api.questers.data;

import fr.skytasul.quests.api.pools.QuestPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface QuesterPoolData {

	int getPoolId();

	@Nullable
	QuestPool getPool();

	long getLastGive();

	void setLastGive(long lastGive);

	@NotNull
	@UnmodifiableView
	Set<@NotNull Integer> getCompletedQuests();

	void setCompletedQuests(@NotNull Set<Integer> completedQuests);

	@NotNull
	CompletableFuture<Void> remove();

}
