package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

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

	public static class Creator implements RequirementCreationRunnables<QuestRequirement> {

		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			Utils.sendMessage(p, Lang.CHOOSE_NPC_STARTER.toString());
			Editor.enterOrLeave(p, new SelectNPC(p, (npc) -> {
				if (npc == null) {
					gui.reopen(p, true);
					gui.removeRequirement(datas);
					return;
				}
				if (QuestsAPI.isQuestStarter(npc)) {
					Inventories.create(p, new ChooseQuestGUI(QuestsAPI.getQuestsAssigneds(npc), (quest) -> {
						if (quest != null) {
							if (datas.containsKey("id")) datas.remove("id");
							datas.put("id", quest.getID());
						}else gui.remove((int) datas.get("slot"));
						gui.reopen(p, true);
					}));
				}else {
					Utils.sendMessage(p, Lang.NPC_NOT_QUEST.toString());
					gui.reopen(p, true);
					gui.removeRequirement(datas);
				}
			}));
		}

		public QuestRequirement finish(Map<String, Object> datas) {
			QuestRequirement req = new QuestRequirement();
			req.questId = (int) datas.get("id");
			return req;
		}

		public void edit(Map<String, Object> datas, QuestRequirement requirement) {
			datas.put("id", requirement.questId);
		}
	}

}
