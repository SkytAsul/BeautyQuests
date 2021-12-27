package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public class QuestRequirement extends AbstractRequirement {

	public int questId;
	private Quest cached;
	
	public QuestRequirement() {
		this(-1);
	}
	
	public QuestRequirement(int questId) {
		this.questId = questId;
	}
	
	@Override
	public boolean test(Player p) {
		if (exists()) return cached.hasFinished(PlayersManager.getPlayerAccount(p));
		return true;
	}
	
	@Override
	public void sendReason(Player p){
		if (exists()) Lang.REQUIREMENT_QUEST.send(p, cached.getName());
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDQuest.format(exists() ? cached.getName() : questId);
	}

	private boolean exists(){
		cached = QuestsAPI.getQuestFromID(questId);
		return cached != null;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + (exists() ? cached.getName() : Lang.NotSet.toString()), "", Lang.RemoveMid.toString() };
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ChooseQuestGUI(BeautyQuests.getInstance().getQuests(), quest -> {
			this.questId = quest.getID();
			event.updateItemLore(getLore());
			event.reopenGUI();
		}).create(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new QuestRequirement(questId);
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("questID", questId);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		questId = (int) savedDatas.get("questID");
	}
	
}
