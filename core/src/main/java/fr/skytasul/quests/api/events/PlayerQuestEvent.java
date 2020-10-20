package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;

public abstract class PlayerQuestEvent extends PlayerEvent{

	protected Quest qu;
	protected PlayerAccount acc;
	
	public PlayerQuestEvent(Player who, Quest quest){
		super(who);
		this.qu = quest;
		this.acc = PlayersManager.getPlayerAccount(who);
	}

	public Quest getQuest(){
		return qu;
	}
	
	public PlayerAccount getPlayerAccount(){
		return acc;
	}

}
