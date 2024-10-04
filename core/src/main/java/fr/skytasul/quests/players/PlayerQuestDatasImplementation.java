package fr.skytasul.quests.players;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class PlayerQuestDatasImplementation implements PlayerQuestDatas {

	protected final PlayerAccountImplementation acc;
	protected final int questID;

	private int finished;
	private long timer;
	private int branch;
	private int stage;
	protected Map<String, Object> additionalDatas;
	protected StringJoiner questFlow = new StringJoiner(";");

	private Boolean hasDialogsCached = null;

	public PlayerQuestDatasImplementation(PlayerAccountImplementation acc, int questID) {
		this.acc = acc;
		this.questID = questID;
		this.finished = 0;
		this.timer = 0;
		this.branch = -1;
		this.stage = -1;
		this.additionalDatas = new HashMap<>();
	}

	public PlayerQuestDatasImplementation(PlayerAccountImplementation acc, int questID, long timer, int finished, int branch, int stage, Map<String, Object> additionalDatas, String questFlow) {
		this.acc = acc;
		this.questID = questID;
		this.finished = finished;
		this.timer = timer;
		this.branch = branch;
		this.stage = stage;
		this.additionalDatas = additionalDatas == null ? new HashMap<>() : additionalDatas;
		if (questFlow != null) this.questFlow.add(questFlow);
		if (branch != -1 && stage == -1) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Incorrect quest " + questID + " datas for " + acc.debugName());
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
	public boolean isFinished() {
		return finished > 0;
	}

	@Override
	public void incrementFinished() {
		finished++;
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
	}

	@Override
	public int getBranch() {
		return branch;
	}

	@Override
	public void setBranch(int branch) {
		this.branch = branch;
	}

	@Override
	public int getStage() {
		return stage;
	}

	@Override
	public void setStage(int stage) {
		this.stage = stage;
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

	public Map<String, Object> getRawAdditionalDatas() {
		return additionalDatas;
	}

	@Override
	public <T> T getAdditionalData(String key) {
		return (T) additionalDatas.get(key);
	}

	@Override
	public <T> T setAdditionalData(String key, T value) {
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
		return Arrays.stream(getQuestFlow().split(";"))
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
		hasDialogsCached = null;
	}

	@Override
	public void resetQuestFlow() {
		questFlow = new StringJoiner(";");
		hasDialogsCached = null;
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

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("questID", questID);
		if (finished != 0) map.put("timesFinished", finished);
		if (timer != 0) map.put("timer", timer);
		if (branch != -1) map.put("currentBranch", branch);
		if (stage != -1) map.put("currentStage", stage);
		if (!additionalDatas.isEmpty()) map.put("datas", additionalDatas);
		if (questFlow.length() > 0) map.put("questFlow", questFlow.toString());

		return map;
	}

	public static PlayerQuestDatasImplementation deserialize(PlayerAccountImplementation acc, Map<String, Object> map) {
		PlayerQuestDatasImplementation datas = new PlayerQuestDatasImplementation(acc, (int) map.get("questID"));
		if (map.containsKey("finished")) datas.finished = ((boolean) map.get("finished")) ? 1 : 0; // TODO migration 0.19
		if (map.containsKey("timesFinished")) datas.finished = (int) map.get("timesFinished");
		if (map.containsKey("timer")) datas.timer = Utils.parseLong(map.get("timer"));
		if (map.containsKey("currentBranch")) datas.branch = (int) map.get("currentBranch");
		if (map.containsKey("currentStage")) datas.stage = (int) map.get("currentStage");
		if (map.containsKey("datas")) datas.additionalDatas = (Map<String, Object>) map.get("datas");
		if (map.containsKey("questFlow")) datas.questFlow.add((String) map.get("questFlow"));

		for (int i = 0; i < 5; i++) { // TODO migration 0.20
			if (map.containsKey("stage" + i + "datas")) {
				datas.additionalDatas.put("stage" + i, map.get("stage" + i + "datas"));
			}
		}

		return datas;
	}

}
