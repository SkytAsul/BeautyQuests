package fr.skytasul.quests.options;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
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
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}
	
	@Override
	public void detach() {
		super.detach();
		HandlerList.unregisterAll(this);
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
