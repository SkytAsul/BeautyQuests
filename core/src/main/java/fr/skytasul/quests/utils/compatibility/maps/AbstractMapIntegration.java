package fr.skytasul.quests.utils.compatibility.maps;

import org.bukkit.Location;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.options.OptionStarterNPC;

public abstract class AbstractMapIntegration implements QuestsHandler {
	
	@Override
	public final void load() {
		if (isEnabled())
			initializeMarkers(this::initializeQuests);
	}
	
	private void initializeQuests() {
		if (QuestsAPI.getAPI().getQuestsManager() != null)
			QuestsAPI.getAPI().getQuestsManager().getQuests().forEach(this::questLoaded);
	}
	
	@Override
	public void questLoaded(Quest quest) {
		if (!isEnabled())
			return;
		if (!quest.hasOption(OptionStarterNPC.class))
			return;
		if (quest.isHidden(QuestVisibilityLocation.MAPS)) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("No marker created for quest " + quest.getId() + ": quest is hidden");
			return;
		}
		
		Location lc = quest.getOptionValueOrDef(OptionStarterNPC.class).getLocation();
		if (lc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot create map marker for quest #" + quest.getId() + " (" + quest.getName() + ")");
		}else {
			addMarker(quest, lc);
		}
	}
	
	@Override
	public void questUnload(Quest quest) {
		if (!quest.isHidden(QuestVisibilityLocation.MAPS) && quest.hasOption(OptionStarterNPC.class)) removeMarker(quest);
	}
	
	public abstract boolean isEnabled();

	protected abstract void initializeMarkers(Runnable initializeQuests);
	
	protected abstract void addMarker(Quest quest, Location lc);
	
	protected abstract void removeMarker(Quest quest);
	
}
