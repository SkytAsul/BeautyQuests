package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;

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

	public HandlerList getHandlers(){
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	  return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
