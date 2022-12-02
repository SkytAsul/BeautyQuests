package fr.skytasul.quests.api.stages.types;

import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.DialogRunner;

public interface Dialogable {
	
	Dialog getDialog();
	
	DialogRunner getDialogRunner();
	
	BQNPC getNPC();
	
	default boolean hasDialog() {
		return getNPC() != null && getDialog() != null && !getDialog().messages.isEmpty();
	}
	
}
