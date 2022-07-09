package fr.skytasul.quests.players;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Utils;

public class PlayerQuestDatas {

	protected final PlayerAccount acc;
	protected final int questID;

	private int finished;
	private long timer;
	private int branch;
	private int stage;
	protected Map<String, Object> additionalDatas;
	protected StringJoiner questFlow = new StringJoiner(";");
	
	private Boolean hasDialogsCached = null;

	public PlayerQuestDatas(PlayerAccount acc, int questID) {
		this.acc = acc;
		this.questID = questID;
		this.finished = 0;
		this.timer = 0;
		this.branch = -1;
		this.stage = -1;
		this.additionalDatas = new HashMap<>();
	}

	public PlayerQuestDatas(PlayerAccount acc, int questID, long timer, int finished, int branch, int stage, Map<String, Object> additionalDatas, String questFlow) {
		this.acc = acc;
		this.questID = questID;
		this.finished = finished;
		this.timer = timer;
		this.branch = branch;
		this.stage = stage;
		this.additionalDatas = additionalDatas == null ? new HashMap<>() : additionalDatas;
		if (questFlow != null) this.questFlow.add(questFlow);
		if (branch != -1 && stage == -1) BeautyQuests.logger.warning("Incorrect quest " + questID + " datas for " + acc.debugName());
	}
	
	public Quest getQuest() {
		return QuestsAPI.getQuests().getQuest(questID);
	}
	
	public int getQuestID() {
		return questID;
	}

	public boolean isFinished() {
		return finished > 0;
	}

	public void incrementFinished() {
		finished++;
	}
	
	public int getTimesFinished() {
		return finished;
	}

	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}

	public int getBranch() {
		return branch;
	}

	public void setBranch(int branch) {
		this.branch = branch;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public boolean hasStarted() {
		return branch != -1;
	}
	
	public boolean isInQuestEnd() {
		return branch == -2;
	}
	
	public void setInQuestEnd() {
		setBranch(-2);
	}

	public boolean isInEndingStages() {
		return stage == -2;
	}

	public void setInEndingStages() {
		setStage(-2);
	}

	public <T> T getAdditionalData(String key) {
		return (T) additionalDatas.get(key);
	}
	
	public <T> T setAdditionalData(String key, T value) {
		return (T) additionalDatas.put(key, value);
	}

	public Map<String, Object> getStageDatas(int stage) {
		return getAdditionalData("stage" + stage);
	}
	
	public void setStageDatas(int stage, Map<String, Object> datas) {
		setAdditionalData("stage" + stage, datas);
	}
	
	public String getQuestFlow() {
		return questFlow.toString();
	}
	
	public void addQuestFlow(AbstractStage finished) {
		String stageID;
		if (finished.getQuestBranch().isRegularStage(finished)) {
			stageID = Integer.toString(finished.getID());
		}else {
			stageID = "E" + finished.getStoredID();
		}
		questFlow.add(finished.getQuestBranch().getID() + ":" + stageID);
		hasDialogsCached = null;
	}
	
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

	public static PlayerQuestDatas deserialize(PlayerAccount acc, Map<String, Object> map) {
		PlayerQuestDatas datas = new PlayerQuestDatas(acc, (int) map.get("questID"));
		if (map.containsKey("finished")) datas.finished = ((boolean) map.get("finished")) ? 1 : 0; // TODO remove, outdated since 0.19
		if (map.containsKey("timesFinished")) datas.finished = (int) map.get("timesFinished");
		if (map.containsKey("timer")) datas.timer = Utils.parseLong(map.get("timer"));
		if (map.containsKey("currentBranch")) datas.branch = (int) map.get("currentBranch");
		if (map.containsKey("currentStage")) datas.stage = (int) map.get("currentStage");
		if (map.containsKey("datas")) datas.additionalDatas = (Map<String, Object>) map.get("datas");
		if (map.containsKey("questFlow")) datas.questFlow.add((String) map.get("questFlow"));
		
		for (int i = 0; i < 5; i++) { // TODO remove ; migration purpose ; added on 0.20
			if (map.containsKey("stage" + i + "datas")) {
				datas.additionalDatas.put("stage" + i, map.get("stage" + i + "datas"));
			}
		}
		
		return datas;
	}

}
