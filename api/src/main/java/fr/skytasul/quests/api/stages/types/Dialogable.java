package fr.skytasul.quests.api.stages.types;

import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;

public interface Dialogable {
	
	@Nullable
	Dialog getDialog();
	
	@Nullable
	DialogRunner getDialogRunner();
	
	@Nullable
	BqNpc getNPC();
	
	default boolean hasDialog() {
		return getNPC() != null && getDialog() != null && !getDialog().getMessages().isEmpty();
	}
	
}
