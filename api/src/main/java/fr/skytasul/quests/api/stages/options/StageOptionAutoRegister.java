package fr.skytasul.quests.api.stages.options;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;

public interface StageOptionAutoRegister {

	boolean appliesTo(@NotNull StageType<?> type);

	<T extends AbstractStage> StageOptionCreator<T> createOptionCreator(@NotNull StageType<T> type);

}
