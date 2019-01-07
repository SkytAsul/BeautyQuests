package fr.skytasul.quests.api.events;

import java.util.List;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;

public class QuestRemoveEvent extends QuestEvent{

	private List<PlayerAccount> launched;
	
	public QuestRemoveEvent(Quest quest, List<PlayerAccount> accounts){
		super(quest);
		this.launched = accounts;
	}
	
	public List<PlayerAccount> getLaunchedAccounts(){
		return launched;
	}

}
