package fr.skytasul.quests.api;

import org.bukkit.Location;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;

public abstract class AbstractMapIntegration implements QuestsHandler {
	
	@Override
	public final void load() {
		if (isEnabled())
			initializeMarkers(this::initializeQuests);
	}
	
	private void initializeQuests() {
		QuestsAPI.getAPI().getQuestsManager().getQuests().forEach(this::questLoaded);
	}
	
	@Override
	public void questLoaded(Quest quest) {
		if (!isEnabled())
			return;

		BqNpc starter = quest.getStarterNpc();
		if (starter == null)
			return;
		if (quest.isHidden(QuestVisibilityLocation.MAPS)) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("No marker created for quest " + quest.getId() + ": quest is hidden");
			return;
		}
		
		Location lc = starter.getLocation();
		if (lc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot create map marker for quest #" + quest.getId() + " (" + quest.getName() + ")");
		}else {
			addMarker(quest, lc);
		}
	}
	
	@Override
	public void questUnload(Quest quest) {
		if (!quest.isHidden(QuestVisibilityLocation.MAPS) && quest.getStarterNpc() != null)
			removeMarker(quest);
	}
	
	public abstract boolean isEnabled();

	protected abstract void initializeMarkers(Runnable initializeQuests);
	
	protected abstract void addMarker(Quest quest, Location lc);
	
	protected abstract void removeMarker(Quest quest);
	
}
