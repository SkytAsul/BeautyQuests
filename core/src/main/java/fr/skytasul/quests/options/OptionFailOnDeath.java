package fr.skytasul.quests.options;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.stages.StageDeath;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class OptionFailOnDeath extends QuestOptionBoolean implements Listener {
	
	@Override
	public String getName() {
		return Lang.failOnDeath.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.failOnDeathLore.toString();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent e) {
		Quester acc = PlayerManager.getPlayerAccount(e.getEntity());
		if (acc == null) return;

		var acknowledgedDeathEvent = StageDeath.ACKNOWLEDGED_DEATHS.getIfPresent(getAttachedQuest());
		if (acknowledgedDeathEvent == e) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(
					"Ignoring death of {0} for quest {1} since it was acknowledged by a death stage.",
					e.getPlayer().getName(), getAttachedQuest().getId());
			return;
		} else if (acknowledgedDeathEvent != null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(
					"A stage acknowledged the death of {0} in quest {1} but the event is mismatched. Something is wrong.",
					e.getPlayer().getName(), getAttachedQuest().getId());
		}

		if (getAttachedQuest().hasStarted(acc)) {
			getAttachedQuest().cancelQuester(acc);
			Lang.QUEST_FAILED.send(e.getEntity(), getAttachedQuest());
		}
	}
	
}
