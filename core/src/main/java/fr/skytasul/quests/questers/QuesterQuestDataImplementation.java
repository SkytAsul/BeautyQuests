package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.questers.data.QuesterQuestDataHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class QuesterQuestDataImplementation implements QuesterQuestData {

	private static final Pattern FLOW_PATTERN = Pattern.compile(";");

	protected final @NotNull Quester quester;
	protected final @NotNull QuesterQuestDataHandler dataHandler;
	protected final int questID;

	private int finished = 0;
	private long timer = 0;
	private int branch = -1;
	private int stage = -1;
	protected Map<String, Object> additionalDatas = new HashMap<>();
	protected StringJoiner questFlow = new StringJoiner(";");

	private Boolean hasDialogsCached = null;

	public QuesterQuestDataImplementation(@NotNull Quester quester, @NotNull QuesterQuestDataHandler dataHandler,
			int questID) {
		this.quester = quester;
		this.dataHandler = dataHandler;
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

	@Override
	public @NotNull Quester getQuester() {
		return quester;
	}

	@Override
	public boolean isFinished() {
		return finished > 0;
	}

	public void setTimesFinished(int times) {
		finished = times;
		dataHandler.setTimesFinished(times);
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
	public long getTimer() {
		return timer;
	}

	@Override
	public void setTimer(long timer) {
		this.timer = timer;
		dataHandler.setTimer(timer);
	}

	@Override
	public int getBranch() {
		return branch;
	}

	@Override
	public void setBranch(int branch) {
		this.branch = branch;
		dataHandler.setBranch(branch);
	}

	@Override
	public int getStage() {
		return stage;
	}

	@Override
	public void setStage(int stage) {
		this.stage = stage;
		dataHandler.setStage(stage);
	}

	@Override
	public boolean hasStarted() {
		return branch != -1;
	}

	@Override
	public boolean isInQuestEnd() {
		return branch == -2;
	}

	@Override
	public void setInQuestEnd() {
		setBranch(-2);
	}

	@Override
	public boolean isInEndingStages() {
		return stage == -2;
	}

	@Override
	public void setInEndingStages() {
		setStage(-2);
	}

	public void loadRawData(@NotNull ConfigurationSection data) {
		additionalDatas = data.getValues(false);
	}

	@Override
	public <T> T getAdditionalData(String key) {
		return (T) additionalDatas.get(key);
	}

	@Override
	public <T> T setAdditionalData(String key, T value) {
		dataHandler.setAdditionalData(key, value);
		return (T) (value == null ? additionalDatas.remove(key) : additionalDatas.put(key, value));
	}

	@Override
	public Map<String, Object> getStageDatas(int stage) {
		return getAdditionalData("stage" + stage);
	}

	@Override
	public void setStageDatas(int stage, Map<String, Object> datas) {
		setAdditionalData("stage" + stage, datas);
	}

	@Override
	public long getStartingTime() {
		return getAdditionalData("starting_time");
	}

	@Override
	public void setStartingTime(long time) {
		setAdditionalData("starting_time", time == 0 ? null : time);
	}

	@Override
	public String getQuestFlow() {
		return questFlow.toString();
	}

	@Override
	public Stream<StageController> getQuestFlowStages() {
		return FLOW_PATTERN.splitAsStream(getQuestFlow())
				.filter(x -> !x.isEmpty())
				.map(arg -> {
					String[] args = arg.split(":");
					int branchID = Integer.parseInt(args[0]);
					QuestBranch branch = getQuest().getBranchesManager().getBranch(branchID);
					if (branch == null)
						return null;
					if (args[1].startsWith("E")) {
						return branch.getEndingStage(Integer.parseInt(args[1].substring(1)));
					} else {
						return branch.getRegularStage(Integer.parseInt(args[1]));
					}
				});
	}

	@Override
	public void addQuestFlow(StageController finished) {
		questFlow.add(finished.getBranch().getId() + ":" + finished.getFlowId());
		dataHandler.setQuestFlow(questFlow.toString());
		hasDialogsCached = null;
	}

	@Override
	public void resetQuestFlow() {
		questFlow = new StringJoiner(";");
		dataHandler.setQuestFlow(questFlow.toString());
		hasDialogsCached = null;
	}

	public void setQuestFlow(@NotNull String flow) {
		questFlow = new StringJoiner(";");
		questFlow.add(flow);
	}

	public boolean hasFlowDialogs() {
		if (hasDialogsCached == null) {
			Quest quest = getQuest();
			hasDialogsCached = quest.hasOption(OptionStartDialog.class) || DialogHistoryGUI.getDialogableStream(this, quest).findAny().isPresent();
		}
		return hasDialogsCached.booleanValue();
	}

	public void questEdited() {
		hasDialogsCached = null;
	}

}
