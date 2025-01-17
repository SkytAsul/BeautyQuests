package fr.skytasul.quests.questers.data;

import fr.skytasul.quests.questers.QuesterQuestDataImplementation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public interface QuesterQuestDataHandler {

	void load(@NotNull QuesterQuestDataImplementation questData);

	CompletableFuture<Void> remove();

	void setTimesFinished(int times);

	void setTimer(long timer);

	void setBranch(int branch);

	void setStage(int stage);

	void setAdditionalData(@NotNull String key, @Nullable Object value);

	void setQuestFlow(@NotNull String flow);

}
