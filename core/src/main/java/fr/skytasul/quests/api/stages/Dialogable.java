package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.DialogRunner;

public interface Dialogable {
	
	Dialog getDialog();
	
	DialogRunner getDialogRunner();
	
	BQNPC getNPC();
	
	default boolean hasDialog() {
		return getDialog() != null;
	}
	
}
