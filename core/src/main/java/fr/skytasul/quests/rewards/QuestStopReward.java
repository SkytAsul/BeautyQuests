package fr.skytasul.quests.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;

public class QuestStopReward extends AbstractReward {
	
	public QuestStopReward() {}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {}
	
	@Override
	public List<String> give(Player p) {
		Quest quest = getAttachedQuest();
		if (quest != null) quest.cancelPlayer(PlayersManager.getPlayerAccount(p));
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return this;
	}
	
	@Override
	protected void save(Map<String, Object> datas) {}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {}
	
}
