package fr.skytasul.quests.players;

import java.util.HashMap;
import java.util.Map;

import fr.skytasul.quests.utils.Utils;

public class PlayerQuestDatas {

	public final PlayerAccount acc;
	public final int questID;

	private boolean finished = false;
	private long timer = 0;
	private int branch = -1, stage = -1;
	private Map<String, Object>[] stageDatas = new Map[5];

	public PlayerQuestDatas(PlayerAccount acc, int questID) {
		this.acc = acc;
		this.questID = questID;
	}

	public PlayerQuestDatas(PlayerAccount acc, int questID, long timer, boolean finished, int branch, int stage, Map<String, Object> stage0datas, Map<String, Object> stage1datas, Map<String, Object> stage2datas, Map<String, Object> stage3datas, Map<String, Object> stage4datas) {
		this.acc = acc;
		this.questID = questID;
		this.finished = finished;
		this.timer = timer;
		this.branch = branch;
		this.stage = stage;
		this.stageDatas[0] = stage0datas;
		this.stageDatas[1] = stage1datas;
		this.stageDatas[2] = stage2datas;
		this.stageDatas[3] = stage3datas;
		this.stageDatas[4] = stage4datas;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
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

	public boolean isInEndingStages() {
		return stage == -2;
	}

	public void setInEndingStages() {
		this.stage = -2;
	}

	public Map<String, Object> getStageDatas(int stage) {
		return stageDatas[stage];
	}

	public void setStageDatas(int stage, Map<String, Object> stageDatas) {
		this.stageDatas[stage] = stageDatas;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("questID", questID);
		if (finished) map.put("finished", finished);
		if (timer != 0) map.put("timer", timer);
		if (branch != -1) map.put("currentBranch", branch);
		if (stage != -1) map.put("currentStage", stage);
		for (int i = 0; i < stageDatas.length; i++) {
			if (stageDatas[i] != null) map.put("stage" + i + "datas", stageDatas[i]);
		}

		return map;
	}

	public static PlayerQuestDatas deserialize(PlayerAccount acc, Map<String, Object> map) {
		PlayerQuestDatas datas = new PlayerQuestDatas(acc, (int) map.get("questID"));
		if (map.containsKey("finished")) datas.finished = (boolean) map.get("finished");
		if (map.containsKey("timer")) datas.timer = Utils.parseLong(map.get("timer"));
		if (map.containsKey("currentBranch")) datas.branch = (int) map.get("currentBranch");
		if (map.containsKey("currentStage")) datas.stage = (int) map.get("currentStage");
		for (int i = 0; i < datas.stageDatas.length; i++) {
			datas.stageDatas[i] = (Map<String, Object>) map.get("stage" + i + "datas");
		}
		return datas;
	}

}
