package fr.skytasul.quests.utils.compatibility.maps;

import org.bukkit.Location;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.options.OptionVisibility.VisibilityLocation;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;

public abstract class AbstractMapIntegration implements QuestsHandler {
	
	@Override
	public final void load() {
		if (isEnabled())
			initializeMarkers(this::initializeQuests);
	}
	
	private void initializeQuests() {
		if (QuestsAPI.getQuests() != null) QuestsAPI.getQuests().forEach(this::questLoaded);
	}
	
	@Override
	public void questLoaded(Quest quest) {
		if (!isEnabled())
			return;
		if (!quest.hasOption(OptionStarterNPC.class))
			return;
		if (quest.isHidden(VisibilityLocation.MAPS)) {
			DebugUtils.logMessage("No marker created for quest " + quest.getID() + ": quest is hidden");
			return;
		}
		
		Location lc = quest.getOptionValueOrDef(OptionStarterNPC.class).getLocation();
		if (lc == null) {
			BeautyQuests.logger.warning("Cannot create map marker for quest #" + quest.getID() + " (" + quest.getName() + ")");
		}else {
			addMarker(quest, lc);
		}
	}
	
	@Override
	public void questUnload(Quest quest) {
		if (!quest.isHidden(VisibilityLocation.MAPS) && quest.hasOption(OptionStarterNPC.class)) removeMarker(quest);
	}
	
	public abstract boolean isEnabled();

	protected abstract void initializeMarkers(Runnable initializeQuests);
	
	protected abstract void addMarker(Quest quest, Location lc);
	
	protected abstract void removeMarker(Quest quest);
	
}
