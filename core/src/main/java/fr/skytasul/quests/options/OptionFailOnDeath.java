package fr.skytasul.quests.options;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;

public class OptionFailOnDeath extends QuestOptionBoolean implements Listener {
	
	@Override
	public String getName() {
		return Lang.failOnDeath.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.failOnDeathLore.toString();
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(e.getEntity());
		if (acc == null) return;
		if (getAttachedQuest().hasStarted(acc)) {
			getAttachedQuest().cancelPlayer(acc);
			Lang.QUEST_FAILED.send(e.getEntity(), getAttachedQuest().getName());
		}
	}
	
}
