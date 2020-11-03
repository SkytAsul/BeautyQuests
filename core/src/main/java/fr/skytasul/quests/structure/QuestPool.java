package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;

public class QuestPool {
	
	private List<Quest> quests = new ArrayList<>();
	private int maxQuests = 1;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	
	private long lastGive = 0;
	
	public void give(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		List<Quest> available = quests.stream().filter(quest -> !quest.hasStarted(acc) && !quest.hasFinished(acc)).collect(Collectors.toList());
		if (!available.isEmpty()) {
			
		}else {
			
		}
	}
	
}
