package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;

public class QuestRequirement extends AbstractRequirement {

	public int questId;
	private Quest cached;
	
	public QuestRequirement() {
		super("questRequired");
	}
	
	public boolean test(Player p) {
		if (exists()) return cached.hasFinished(PlayersManager.getPlayerAccount(p));
		return true;
	}
	
	public void sendReason(Player p){
		if (exists() && QuestsAPI.getQuestsAssigneds(quest.getStarter()).size() == 1){
			Lang.REQUIREMENT_QUEST.send(p, cached.getName());
		}
	}

	private boolean exists(){
		cached = QuestsAPI.getQuestFromID(questId);
		return cached != null;
	}
	
	
	protected void save(Map<String, Object> datas) {
		datas.put("questID", questId);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		questId = (int) savedDatas.get("questID");
		//Validate.notNull(QuestsAPI.getQuestFromID(questId), "Quest with id " + questId + " is null");
	}

}
