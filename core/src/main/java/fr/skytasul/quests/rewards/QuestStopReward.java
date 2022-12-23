package fr.skytasul.quests.rewards;

import java.util.List;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.DebugUtils;

public class QuestStopReward extends AbstractReward {
	
	public QuestStopReward() {}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {}
	
	@Override
	public List<String> give(Player p) throws InterruptingBranchException {
		if (getAttachedQuest() == null) {
			DebugUtils.logMessage("No attached quest for " + debugName());
		} else {
			getAttachedQuest().cancelPlayer(PlayersManager.getPlayerAccount(p));
			throw new InterruptingBranchException();
		}
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return new QuestStopReward();
	}
	
}
