package fr.skytasul.quests.api.players;

import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;

public interface PlayerQuestDatas {

	int getQuestID();

	@Nullable
	Quest getQuest();

	boolean isFinished();

	void incrementFinished();

	int getTimesFinished();

	long getTimer();

	void setTimer(long timer);

	int getBranch();

	void setBranch(int branch);

	int getStage();

	void setStage(int stage);

	boolean hasStarted();

	boolean isInQuestEnd();

	void setInQuestEnd();

	boolean isInEndingStages();

	void setInEndingStages();

	<T> @Nullable T getAdditionalData(@NotNull String key);

	<T> @Nullable T setAdditionalData(@NotNull String key, @Nullable T value);

	@Nullable
	Map<@NotNull String, @Nullable Object> getStageDatas(int stage);

	void setStageDatas(int stage, @Nullable Map<@NotNull String, @Nullable Object> datas);

	long getStartingTime();

	void setStartingTime(long time);

	@NotNull
	String getQuestFlow();

	void addQuestFlow(@NotNull StageController finished);

	void resetQuestFlow();

	Stream<StageController> getQuestFlowStages();

}
