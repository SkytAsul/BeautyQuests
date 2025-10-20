package fr.skytasul.quests.gui.creation;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.gui.creation.quest.QuestCreationGuiImplementation;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.structure.QuestImplementation;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestCreationSession {

	private StagesGUI stagesGUI;
	private QuestCreationGuiImplementation creationGUI;

	private final @NotNull Player player;

	private QuestImplementation questEdited;
	private boolean stagesEdited = false;

	private int customID = -1;

	public QuestCreationSession(@NotNull Player player) {
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public @NotNull Audience getPlayerAudience() {
		return QuestsPlugin.getPlugin().getAudiences().player(player);
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

	public QuestImplementation getQuestEdited() {
		return questEdited;
	}

	public void setQuestEdited(QuestImplementation questEdited) {
		this.questEdited = questEdited;
	}

	public void setStagesEdited() {
		stagesEdited = true;
	}

	public boolean areStagesEdited() {
		return isEdition() && stagesEdited;
	}

	public StagesGUI getStagesGUI() {
		return stagesGUI;
	}

	public void openStagesGUI(Player p) {
		if (stagesGUI == null)
			stagesGUI = new StagesGUI(this);
		stagesGUI.open(p);
	}

	public void openCreationGUI(Player p) {
		if (creationGUI == null)
			creationGUI = new QuestCreationGuiImplementation(this);
		creationGUI.open(p);
	}

}
