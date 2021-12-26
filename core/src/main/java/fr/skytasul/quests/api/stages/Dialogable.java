package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.types.Dialog;

public interface Dialogable {
	
	Dialog getDialog();
	
	BQNPC getNPC();
	
	default boolean hasDialog() {
		return getDialog() != null;
	}
	
}
