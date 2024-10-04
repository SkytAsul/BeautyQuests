package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import org.bukkit.entity.Player;
import java.util.List;

public class QuestStopReward extends AbstractReward {
	
	public QuestStopReward() {}
	
	public QuestStopReward(String customDescription) {
		super(customDescription);
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {}
	
	@Override
	public List<String> give(Player p) throws InterruptingBranchException {
		if (getAttachedQuest() == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("No attached quest for " + debugName());
		} else {
			getAttachedQuest().cancelPlayer(PlayersManager.getPlayerAccount(p));
			throw new InterruptingBranchException();
		}
		return null;
	}
	
	@Override
	public AbstractReward clone() {
		return new QuestStopReward(getCustomDescription());
	}
	
}
