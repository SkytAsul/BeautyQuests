package fr.skytasul.quests.api.stages.types;

import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.DialogRunner;

public interface Dialogable {
	
	@Nullable
	Dialog getDialog();
	
	@Nullable
	DialogRunner getDialogRunner();
	
	@Nullable
	BQNPC getNPC();
	
	default boolean hasDialog() {
		return getNPC() != null && getDialog() != null && !getDialog().messages.isEmpty();
	}
	
}
