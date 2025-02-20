package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public abstract class AbstractQuesterQuestDataImplementation implements QuesterQuestData {

	protected final int questID;

	protected int finished = 0;
	protected State state = State.NOT_STARTED;
	protected OptionalLong timer = OptionalLong.empty();
	protected OptionalInt branch = OptionalInt.empty();
	protected OptionalInt stage = OptionalInt.empty();
	protected OptionalLong startingTime = OptionalLong.empty();

	protected Map<Integer, Map<String, Object>> stageData = new HashMap<>();

	protected Map<String, Object> additionalData = new HashMap<>();

	protected final List<StageController> questFlow = new ArrayList<>();
	private final List<StageController> questFlowView = Collections.unmodifiableList(questFlow);

	public AbstractQuesterQuestDataImplementation(int questID) {
		this.questID = questID;
	}

	@Override
	public Quest getQuest() {
		return QuestsAPI.getAPI().getQuestsManager().getQuest(questID);
	}

	@Override
	public int getQuestID() {
		return questID;
	}

	public void setTimesFinished(int times) {
		finished = times;
	}

	@Override
	public void incrementFinished() {
		setTimesFinished(finished + 1);
	}

	@Override
	public int getTimesFinished() {
		return finished;
	}

	@Override
	public void setTimer(@NotNull OptionalLong timer) {
		this.timer = timer;
	}

	@Override
	public OptionalLong getTimer() {
		return timer;
	}

	@Override
	public OptionalInt getBranch() {
		return branch;
	}

	@Override
	public void setBranch(@NotNull OptionalInt branch) {
		this.branch = branch;
	}

	@Override
	public OptionalInt getStage() {
		return stage;
	}

	@Override
	public void setStage(@NotNull OptionalInt stage) {
		this.stage = stage;
	}

	@Override
	public OptionalLong getStartingTime() {
		return startingTime;
	}

	@Override
	public void setStartingTime(@NotNull OptionalLong time) {
		this.startingTime = time;
	}

	@Override
	public @NotNull State getState() {
		return state;
	}

	@Override
	public void setState(@NotNull State state) {
		this.state = state;
	}

	@Override
	public Map<String, Object> getStageDatas(int stage) {
		return stageData.get(stage);
	}

	@Override
	public void setStageDatas(int stage, Map<String, Object> datas) {
		stageData.put(stage, datas);
	}

	@Override
	public <T> T getAdditionalData(String key) {
		return (T) additionalData.get(key);
	}

	@Override
	public <T> T setAdditionalData(String key, T value) {
		return (T) (value == null ? additionalData.remove(key) : additionalData.put(key, value));
	}

	@Override
	public List<StageController> getQuestFlowStages() {
		return questFlowView;
	}

	@Override
	public void addQuestFlow(StageController finished) {
		questFlow.add(finished);
	}

	@Override
	public void resetQuestFlow() {
		questFlow.clear();
	}

	protected void migrateState() {
		// TODO delete, migration 2.0
		State newState = State.NOT_STARTED;
		if (this.branch.isPresent())
			if (this.branch.getAsInt() == -2)
				newState = State.IN_END;
			else if (this.branch.getAsInt() >= 0)
				if (this.stage.isPresent() && this.stage.getAsInt() == -2)
					newState = State.IN_ENDING_STAGES;
				else if (this.stage.isPresent() && this.stage.getAsInt() >= 0)
					newState = State.IN_REGULAR_STAGE;
	
		if (newState != State.IN_REGULAR_STAGE) {
			setStage(OptionalInt.empty()); // must be AFTER loading of current stage and branch!
			setBranch(OptionalInt.empty());
		}
		setState(newState);
	}

}
