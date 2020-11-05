package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayersManager;

public class QuestPool {
	
	private final int id;
	
	private List<Quest> quests = new ArrayList<>();
	private int maxQuests = 1;
	private boolean checkForRedo = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	
	public QuestPool(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public String give(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		if (datas.getLastGive() + timeDiff > System.currentTimeMillis()) return "no time";
		
		List<Quest> notDoneQuests = quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList());
		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!checkForRedo) return "all quests completed";
			notDoneQuests = quests.stream().filter(Quest::isRepeatable).collect(Collectors.toList());
			if (notDoneQuests.isEmpty()) return "all quests completed";
			datas.setCompletedQuests(quests.stream().filter(quest -> !quest.isRepeatable()).map(Quest::getID).collect(Collectors.toList()));
		}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) return "max quest limit";
		
		List<Quest> available = quests.stream().filter(quest -> quest.isLauncheable(p, false)).collect(Collectors.toList());
		if (available.isEmpty()) {
			return "no quest available";
		}else {
			Quest quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
			quest.start(p);
			return "started quest #" + quest.getID();
		}
	}
	
}
