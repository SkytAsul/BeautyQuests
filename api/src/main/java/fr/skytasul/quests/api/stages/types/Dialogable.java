package fr.skytasul.quests.api.stages.types;

import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	default @NotNull String getNpcName() {
		if (getNPC() == null)
			return "§c§lunknown NPC";
		if (getDialog() != null && getDialog().getNpcName() != null)
			return getDialog().getNpcName();
		return getNPC().getNpc().getName();
	}

}
