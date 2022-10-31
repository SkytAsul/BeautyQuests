package fr.skytasul.quests.gui.creation;

import org.bukkit.entity.Player;

import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.structure.Quest;

public class QuestCreationSession {
	
	private StagesGUI mainGUI;
	private FinishGUI finishGUI;
	
	private final Quest questEdited;
	private boolean stagesEdited = false;
	
	private int customID = -1;
	
	public QuestCreationSession() {
		this(null);
	}
	
	public QuestCreationSession(Quest questEdited) {
		this.questEdited = questEdited;
	}
	
	public boolean hasCustomID() {
		return customID != -1;
	}
	
	public int getCustomID() {
		return customID;
	}
	
	public void setCustomID(int customID) {
		this.customID = customID;
	}
	
	public boolean isEdition() {
		return questEdited != null;
	}
	
	public Quest getQuestEdited() {
		return questEdited;
	}
	
	public void setStagesEdited() {
		stagesEdited = true;
	}
	
	public boolean areStagesEdited() {
		return isEdition() && stagesEdited;
	}
	
	public StagesGUI getMainGUI() {
		return mainGUI;
	}
	
	public void openMainGUI(Player p) {
		if (mainGUI == null) mainGUI = new StagesGUI(this);
		Inventories.create(p, mainGUI);
	}
	
	public void openFinishGUI(Player p) {
		if (finishGUI == null) finishGUI = new FinishGUI(this);
		Inventories.create(p, finishGUI);
	}
	
}
