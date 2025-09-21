package fr.skytasul.quests.api.questers.data;

import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface QuesterQuestData {

	int getQuestId();

	@Nullable
	Quest getQuest();

	void incrementFinished();

	int getTimesFinished();

	default boolean hasFinishedOnce() {
		return getTimesFinished() > 0;
	}

	OptionalLong getTimer();

	void setTimer(OptionalLong timer);

	OptionalInt getBranch();

	void setBranch(OptionalInt branch);

	OptionalInt getStage();

	void setStage(OptionalInt stage);

	@NotNull
	State getState();

	void setState(@NotNull State state);

	default boolean hasStarted() {
		return getState() != State.NOT_STARTED;
	}

	@Deprecated
	default boolean isInQuestEnd() {
		return getState() == State.IN_END;
	}

	@Deprecated
	default boolean isInEndingStages() {
		return getState() == State.IN_ENDING_STAGES;
	}

	OptionalLong getStartingTime();

	void setStartingTime(OptionalLong time);

	@NotNull
	@UnmodifiableView
	Map<@NotNull Integer, Map<@NotNull String, @Nullable Object>> getAllStagesData();

	@Nullable
	@UnmodifiableView
	Map<@NotNull String, @Nullable Object> getStageData(int stage);

	void setStageData(int stage, @Nullable Map<@NotNull String, @Nullable Object> datas);

	@NotNull
	@UnmodifiableView
	Map<@NotNull String, @NotNull Object> getAllAdditionalData();

	<T> @Nullable T getAdditionalData(@NotNull String key);

	<T> @Nullable T setAdditionalData(@NotNull String key, @Nullable T value);

	void addQuestFlow(@NotNull StageController finished);

	void resetQuestFlow();

	@NotNull
	@UnmodifiableView
	List<StageIndex> getQuestFlow();

	@NotNull
	Stream<StageController> getQuestFlowStages();

	@NotNull
	CompletableFuture<Void> remove();

	enum State {
		NOT_STARTED, IN_REGULAR_STAGE, IN_ENDING_STAGES, IN_END;
	}

}
