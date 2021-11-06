package fr.skytasul.quests.api;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;

public class QuestsHandler {
	
	public void load() {
		// can be implemented
	}
	
	public void unload() {
		// can be implemented
	}
	
	public void stageStart(PlayerAccount acc, AbstractStage stage) {
		// can be implemented
	}
	
	public void stageEnd(PlayerAccount acc, AbstractStage stage) {
		// can be implemented
	}
	
	public void stageJoin(PlayerAccount acc, AbstractStage stage) {
		// can be implemented
	}
	
	public void stageLeave(PlayerAccount acc, AbstractStage stage) {
		// can be implemented
	}
	
	public void stageLoad(AbstractStage stage) {
		// can be implemented
	}
	
	public void stageUnload(AbstractStage stage) {
		// can be implemented
	}
	
}
